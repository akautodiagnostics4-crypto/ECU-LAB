package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BtDeviceItem(
    val name: String,
    val address: String,
    val isPaired: Boolean,
    val isHc05Candidate: Boolean,
    val rssi: Int? = null
)

sealed class BtConnectionState {
    object Disconnected : BtConnectionState()
    data class Scanning(val foundCount: Int) : BtConnectionState()
    data class Pairing(val deviceName: String, val address: String) : BtConnectionState()
    data class Connecting(val deviceName: String, val address: String) : BtConnectionState()
    data class Connected(val deviceName: String, val address: String) : BtConnectionState()
    data class Error(val message: String) : BtConnectionState()
}

/**
 * Full-featured Bluetooth Classic SPP Connection Manager for Arduino Uno + HC-05 (9600 baud).
 * Supports:
 * - Paired device enumeration
 * - Active discovery scanning for nearby unpaired HC-05 / HC-06 modules
 * - In-app pairing (`createBond()`) with broadcast state monitoring
 * - Automatic & manual HC-05 connection (standard SPP UUID + fallback RFCOMM channel 1)
 * - Auto-reconnect option and bi-directional serial TX/RX telemetry stream
 */
class Hc05BluetoothController(private val context: Context) {

    companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var activeSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var readerJob: Job? = null
    private var lastConnectedDevice: BtDeviceItem? = null
    private var receiverRegistered = false

    private val _connectionState = MutableStateFlow<BtConnectionState>(BtConnectionState.Disconnected)
    val connectionState: StateFlow<BtConnectionState> = _connectionState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BtDeviceItem>>(emptyList())
    val pairedDevices: StateFlow<List<BtDeviceItem>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BtDeviceItem>>(emptyList())
    val discoveredDevices: StateFlow<List<BtDeviceItem>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _autoReconnect = MutableStateFlow(true)
    val autoReconnect: StateFlow<Boolean> = _autoReconnect.asStateFlow()

    private val _txLog = MutableStateFlow<List<String>>(emptyList())
    val txLog: StateFlow<List<String>> = _txLog.asStateFlow()

    private val _packetsSentCount = MutableStateFlow(0)
    val packetsSentCount: StateFlow<Int> = _packetsSentCount.asStateFlow()

    private val _packetsReceivedCount = MutableStateFlow(0)
    val packetsReceivedCount: StateFlow<Int> = _packetsReceivedCount.asStateFlow()

    private val _lastArduinoAck = MutableStateFlow<String?>(null)
    val lastArduinoAck: StateFlow<String?> = _lastArduinoAck.asStateFlow()

    private val btBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                    if (device != null && hasBluetoothPermissions()) {
                        val devName = device.name ?: "Unnamed BT (${device.address.takeLast(5)})"
                        val isBonded = device.bondState == BluetoothDevice.BOND_BONDED
                        val item = BtDeviceItem(
                            name = devName,
                            address = device.address,
                            isPaired = isBonded,
                            isHc05Candidate = isHc05Name(devName),
                            rssi = if (rssi != Short.MIN_VALUE.toInt()) rssi else null
                        )
                        if (isBonded) {
                            refreshPairedDevices()
                        } else {
                            val current = _discoveredDevices.value.toMutableList()
                            val existingIndex = current.indexOfFirst { it.address == item.address }
                            if (existingIndex >= 0) {
                                current[existingIndex] = item
                            } else {
                                current.add(item)
                            }
                            _discoveredDevices.value = current.sortedByDescending { it.isHc05Candidate }
                            if (_connectionState.value is BtConnectionState.Scanning) {
                                _connectionState.value = BtConnectionState.Scanning(_discoveredDevices.value.size)
                            }
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                    if (_connectionState.value !is BtConnectionState.Connected) {
                        _connectionState.value = BtConnectionState.Scanning(_discoveredDevices.value.size)
                    }
                    appendLog("SCAN -> Searching for nearby HC-05 / Arduino modules...")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                    if (_connectionState.value is BtConnectionState.Scanning) {
                        _connectionState.value = BtConnectionState.Disconnected
                    }
                    appendLog("SCAN -> Discovery finished (${_discoveredDevices.value.size} new devices)")
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    if (device != null && hasBluetoothPermissions()) {
                        val devName = device.name ?: device.address
                        when (bondState) {
                            BluetoothDevice.BOND_BONDING -> {
                                _connectionState.value = BtConnectionState.Pairing(devName, device.address)
                                appendLog("PAIRING -> Bonding with $devName (Default PIN: 1234 or 0000)")
                            }
                            BluetoothDevice.BOND_BONDED -> {
                                appendLog("PAIRED -> $devName bonded! Connecting SPP socket...")
                                refreshPairedDevices()
                                _discoveredDevices.value = _discoveredDevices.value.filterNot { it.address == device.address }
                                connectToDevice(
                                    BtDeviceItem(
                                        name = devName,
                                        address = device.address,
                                        isPaired = true,
                                        isHc05Candidate = isHc05Name(devName)
                                    )
                                )
                            }
                            BluetoothDevice.BOND_NONE -> {
                                if (_connectionState.value is BtConnectionState.Pairing) {
                                    _connectionState.value = BtConnectionState.Error("Pairing rejected or timed out for $devName (Try PIN 1234)")
                                    appendLog("ERR -> Pairing failed with $devName")
                                }
                            }
                        }
                    }
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val current = _connectionState.value
                    if (current is BtConnectionState.Connected) {
                        disconnectInternal()
                        _connectionState.value = BtConnectionState.Disconnected
                        appendLog("LINK -> HC-05 disconnected (${current.deviceName})")
                        val lastDev = lastConnectedDevice
                        if (_autoReconnect.value && lastDev != null) {
                            scope.launch {
                                delay(2500L)
                                if (_connectionState.value !is BtConnectionState.Connected) {
                                    appendLog("AUTO-RECONNECT -> Retrying ${lastDev.name}...")
                                    connectToDevice(lastDev)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        registerReceiverIfNeeded()
        refreshPairedDevices()
    }

    private fun registerReceiverIfNeeded() {
        if (!receiverRegistered) {
            try {
                val filter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                    addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                    addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(btBroadcastReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(btBroadcastReceiver, filter)
                }
                receiverRegistered = true
            } catch (_: Exception) {}
        }
    }

    private fun isHc05Name(name: String): Boolean {
        return name.contains("HC-05", ignoreCase = true) ||
            name.contains("HC-06", ignoreCase = true) ||
            name.contains("HC05", ignoreCase = true) ||
            name.contains("ECU", ignoreCase = true) ||
            name.contains("ARDUINO", ignoreCase = true)
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasScanPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun setAutoReconnect(enabled: Boolean) {
        _autoReconnect.value = enabled
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            _pairedDevices.value = emptyList()
            return
        }
        try {
            val bonded: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices ?: emptySet()
            val items = bonded.map { device ->
                val devName = device.name ?: "Unknown Device"
                BtDeviceItem(
                    name = devName,
                    address = device.address,
                    isPaired = true,
                    isHc05Candidate = isHc05Name(devName)
                )
            }.sortedByDescending { it.isHc05Candidate }
            _pairedDevices.value = items
        } catch (_: SecurityException) {
            _pairedDevices.value = emptyList()
        }
    }

    /**
     * Starts active Bluetooth Classic discovery to find nearby unpaired HC-05 modules.
     */
    @SuppressLint("MissingPermission")
    fun startDiscoveryScan() {
        registerReceiverIfNeeded()
        refreshPairedDevices()
        if (!hasScanPermissions()) {
            _connectionState.value = BtConnectionState.Error("Bluetooth Scan permission is required to discover HC-05")
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BtConnectionState.Error("Bluetooth is turned off. Please enable Bluetooth.")
            return
        }
        try {
            _discoveredDevices.value = emptyList()
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            val started = bluetoothAdapter.startDiscovery()
            if (!started) {
                _isScanning.value = false
                appendLog("WARN -> Discovery scan could not start (check Location/Bluetooth service)")
            }
        } catch (e: SecurityException) {
            _connectionState.value = BtConnectionState.Error("Bluetooth scan permission denied")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscoveryScan() {
        if (hasScanPermissions() && bluetoothAdapter?.isDiscovering == true) {
            try {
                bluetoothAdapter.cancelDiscovery()
            } catch (_: SecurityException) {}
        }
        _isScanning.value = false
    }

    /**
     * Quick Connect: Automatically finds the best paired HC-05 / Arduino module and connects,
     * or initiates a discovery scan if none is paired yet.
     */
    fun quickConnectHc05() {
        refreshPairedDevices()
        val pairedList = _pairedDevices.value
        val candidate = pairedList.firstOrNull { it.isHc05Candidate } ?: pairedList.firstOrNull()
        if (candidate != null) {
            connectToDevice(candidate)
        } else {
            startDiscoveryScan()
        }
    }

    /**
     * Initiates OS-level Bluetooth pairing (`createBond()`) with an unpaired HC-05 device,
     * then automatically connects once bonded.
     */
    @SuppressLint("MissingPermission")
    fun pairAndConnectDevice(deviceItem: BtDeviceItem) {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            _connectionState.value = BtConnectionState.Error("Bluetooth permission required to pair with HC-05")
            return
        }
        try {
            stopDiscoveryScan()
            val remoteDevice = bluetoothAdapter.getRemoteDevice(deviceItem.address)
            if (remoteDevice.bondState == BluetoothDevice.BOND_BONDED) {
                connectToDevice(deviceItem.copy(isPaired = true))
            } else {
                _connectionState.value = BtConnectionState.Pairing(deviceItem.name, deviceItem.address)
                appendLog("PAIR -> Requesting bond with ${deviceItem.name} [${deviceItem.address}]...")
                val bondInitiated = remoteDevice.createBond()
                if (!bondInitiated) {
                    // Fallback: attempt direct RFCOMM socket connect which triggers system PIN prompt
                    connectToDevice(deviceItem)
                }
            }
        } catch (e: Exception) {
            _connectionState.value = BtConnectionState.Error("Pairing error: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceItem: BtDeviceItem) {
        if (!hasBluetoothPermissions()) {
            _connectionState.value = BtConnectionState.Error("Bluetooth Connect permission required")
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BtConnectionState.Error("Bluetooth is turned off on this device")
            return
        }

        scope.launch {
            stopDiscoveryScan()
            disconnectInternal()
            _connectionState.value = BtConnectionState.Connecting(deviceItem.name, deviceItem.address)
            appendLog("CONNECTING -> ${deviceItem.name} [${deviceItem.address}]...")

            try {
                val device = bluetoothAdapter.getRemoteDevice(deviceItem.address)
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()

                onSocketConnected(socket, deviceItem)
            } catch (e: Exception) {
                // Fallback reflection socket for HC-05 modules using RFCOMM channel 1 directly
                try {
                    val device = bluetoothAdapter.getRemoteDevice(deviceItem.address)
                    val fallbackMethod = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    val fallbackSocket = fallbackMethod.invoke(device, 1) as BluetoothSocket
                    fallbackSocket.connect()

                    onSocketConnected(fallbackSocket, deviceItem)
                } catch (fallbackEx: Exception) {
                    disconnectInternal()
                    _connectionState.value = BtConnectionState.Error(
                        "Could not connect to ${deviceItem.name}. Ensure HC-05 is powered & paired (PIN 1234)."
                    )
                    appendLog("ERR -> Connection failed (${deviceItem.name})")
                }
            }
        }
    }

    private fun onSocketConnected(socket: BluetoothSocket, deviceItem: BtDeviceItem) {
        activeSocket = socket
        outputStream = socket.outputStream
        lastConnectedDevice = deviceItem.copy(isPaired = true)
        _connectionState.value = BtConnectionState.Connected(deviceItem.name, deviceItem.address)
        appendLog("CONNECTED -> ${deviceItem.name} [${deviceItem.address}] @ 9600 baud")
        startIncomingReader(socket)
    }

    private fun startIncomingReader(socket: BluetoothSocket) {
        readerJob?.cancel()
        readerJob = scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.US_ASCII))
                while (isActive && socket.isConnected) {
                    val line = reader.readLine() ?: break
                    val clean = line.trim()
                    if (clean.isNotEmpty()) {
                        _packetsReceivedCount.value += 1
                        _lastArduinoAck.value = clean
                        appendLog("RX <- $clean")
                    }
                }
            } catch (_: IOException) {
                // Socket closed or link lost
            }
        }
    }

    fun disconnect() {
        scope.launch {
            lastConnectedDevice = null
            disconnectInternal()
            _connectionState.value = BtConnectionState.Disconnected
            appendLog("DISCONNECTED -> Manual disconnect")
        }
    }

    private fun disconnectInternal() {
        readerJob?.cancel()
        readerJob = null
        try {
            outputStream?.close()
        } catch (_: IOException) {}
        try {
            activeSocket?.close()
        } catch (_: IOException) {}
        outputStream = null
        activeSocket = null
    }

    /**
     * Sends a diagnostic ping command to Arduino Uno over HC-05.
     */
    fun sendTestPing() {
        scope.launch {
            val stream = outputStream
            if (stream != null && _connectionState.value is BtConnectionState.Connected) {
                try {
                    withContext(Dispatchers.IO) {
                        stream.write("\$PING,HC05_OK\n".toByteArray(Charsets.US_ASCII))
                        stream.flush()
                    }
                    _packetsSentCount.value += 1
                    appendLog("TX -> \$PING,HC05_OK")
                } catch (e: IOException) {
                    disconnectInternal()
                    _connectionState.value = BtConnectionState.Error("HC-05 link lost: ${e.localizedMessage}")
                }
            } else {
                appendLog("WARN -> Cannot send PING (HC-05 not connected)")
            }
        }
    }

    /**
     * Transmits live ECU parameters to Arduino Uno over HC-05 Bluetooth SPP (9600 baud).
     * Note: While the raw crank pattern is sent inside the byte stream to Arduino Uno's timer
     * interrupt generator, the UI log masks the tooth pattern per user instructions ("DONT SHOW
     * THE CRANK TOOTH PATTERN ON THE DISPLAY").
     */
    fun sendEcuFrame(
        internalCrankPattern: String,
        rpm: Int,
        railBar: Int,
        ectCelsius: Int,
        speedKmh: Int,
        accelPercent: Int,
        isRunning: Boolean
    ) {
        val runFlag = if (isRunning) 1 else 0
        val wirePacket = "\$$internalCrankPattern,$rpm,$railBar,$ectCelsius,$speedKmh,$accelPercent,$runFlag\n"
        val displayPacket = "\$RPM:$rpm,RAIL:$railBar,ECT:$ectCelsius,SPD:$speedKmh,ACC:$accelPercent,RUN:$runFlag"

        scope.launch {
            val stream = outputStream
            if (stream != null && _connectionState.value is BtConnectionState.Connected) {
                try {
                    withContext(Dispatchers.IO) {
                        stream.write(wirePacket.toByteArray(Charsets.US_ASCII))
                        stream.flush()
                    }
                    _packetsSentCount.value += 1
                    appendLog("TX -> $displayPacket")
                } catch (e: IOException) {
                    disconnectInternal()
                    _connectionState.value = BtConnectionState.Error("HC-05 link lost: ${e.localizedMessage}")
                    appendLog("ERR -> Link lost during TX")
                }
            } else {
                appendLog("BUFFERED (NO HC-05) -> $displayPacket")
            }
        }
    }

    fun clearLogs() {
        _txLog.value = emptyList()
    }

    private fun appendLog(entry: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val current = _txLog.value.toMutableList()
        current.add(0, "[$timestamp] $entry")
        if (current.size > 40) {
            current.removeAt(current.lastIndex)
        }
        _txLog.value = current
    }
}
