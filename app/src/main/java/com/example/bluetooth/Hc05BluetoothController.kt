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

enum class SerialPacketMode(val label: String, val description: String) {
    KeyValueCommands(
        label = "Key=Value Commands (Default)",
        description = "START\\n | STOP\\n | RPM=800\\n | RAIL=450\\n | ECT=80\\n"
    ),
    Extended11Field(
        label = "Extended 11-Ch CSV Frame",
        description = "\$PROFILE,RPM,RAIL,ECT,SPD,ACC,MAP,MAF,CAM,INJ,RUN"
    ),
    Standard6Field(
        label = "Standard 6-Ch CSV Frame",
        description = "\$PROFILE,RPM,ACCEL,RAIL,ECT,RUN"
    )
}

/**
 * Bluetooth Connection Manager for ECU LAB.
 * Sanitizes technical hardware names (e.g., HC-05, HC-06, Arduino) into friendly
 * "ECU LAB Bluetooth" names so technical hardware naming conventions are completely hidden.
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

    private val _packetMode = MutableStateFlow(SerialPacketMode.KeyValueCommands)
    val packetMode: StateFlow<SerialPacketMode> = _packetMode.asStateFlow()

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
                        val rawName = device.name ?: ""
                        val isTarget = isTargetBtDevice(rawName)
                        val displayName = sanitizeDisplayName(rawName, device.address)
                        val isBonded = device.bondState == BluetoothDevice.BOND_BONDED
                        val item = BtDeviceItem(
                            name = displayName,
                            address = device.address,
                            isPaired = isBonded,
                            isHc05Candidate = isTarget,
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
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                    if (_connectionState.value is BtConnectionState.Scanning) {
                        _connectionState.value = BtConnectionState.Disconnected
                    }
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
                        val rawName = device.name ?: ""
                        val displayName = sanitizeDisplayName(rawName, device.address)
                        when (bondState) {
                            BluetoothDevice.BOND_BONDING -> {
                                _connectionState.value = BtConnectionState.Pairing(displayName, device.address)
                            }
                            BluetoothDevice.BOND_BONDED -> {
                                refreshPairedDevices()
                                _discoveredDevices.value = _discoveredDevices.value.filterNot { it.address == device.address }
                                connectToDevice(
                                    BtDeviceItem(
                                        name = displayName,
                                        address = device.address,
                                        isPaired = true,
                                        isHc05Candidate = isTargetBtDevice(rawName)
                                    )
                                )
                            }
                            BluetoothDevice.BOND_NONE -> {
                                if (_connectionState.value is BtConnectionState.Pairing) {
                                    _connectionState.value = BtConnectionState.Error("Pairing canceled or rejected for $displayName")
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
                        val lastDev = lastConnectedDevice
                        if (_autoReconnect.value && lastDev != null) {
                            scope.launch {
                                delay(2500L)
                                if (_connectionState.value !is BtConnectionState.Connected) {
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

    /**
     * Hides technical hardware module names ("HC-05", "HC-06", "Arduino", etc.) and presents
     * clean Bluetooth device names in the discovery and pairing list.
     */
    private fun sanitizeDisplayName(rawName: String?, address: String): String {
        val trimmed = rawName?.trim().orEmpty()
        val suffix = address.replace(":", "").takeLast(4).uppercase(Locale.US)
        if (trimmed.isEmpty()) {
            return "Bluetooth Device ($suffix)"
        }
        val lower = trimmed.lowercase(Locale.US)
        if (lower.contains("hc-05") ||
            lower.contains("hc05") ||
            lower.contains("hc-06") ||
            lower.contains("hc06") ||
            lower.contains("arduino") ||
            lower.contains("bt04") ||
            lower.contains("jdym")
        ) {
            return "ECU LAB Bluetooth ($suffix)"
        }
        return trimmed
    }

    private fun isTargetBtDevice(rawName: String?): Boolean {
        val name = rawName.orEmpty()
        return name.contains("HC", ignoreCase = true) ||
            name.contains("BT", ignoreCase = true) ||
            name.contains("ECU", ignoreCase = true) ||
            name.contains("LAB", ignoreCase = true) ||
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

    fun setSerialPacketMode(mode: SerialPacketMode) {
        _packetMode.value = mode
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
                val rawName = device.name
                BtDeviceItem(
                    name = sanitizeDisplayName(rawName, device.address),
                    address = device.address,
                    isPaired = true,
                    isHc05Candidate = isTargetBtDevice(rawName)
                )
            }.sortedByDescending { it.isHc05Candidate }
            _pairedDevices.value = items
        } catch (_: SecurityException) {
            _pairedDevices.value = emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscoveryScan() {
        registerReceiverIfNeeded()
        refreshPairedDevices()
        if (!hasScanPermissions()) {
            _connectionState.value = BtConnectionState.Error("Bluetooth permission is required to scan")
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BtConnectionState.Error("Please turn on Bluetooth to scan for devices")
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
            }
        } catch (_: SecurityException) {
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

    @SuppressLint("MissingPermission")
    fun pairAndConnectDevice(deviceItem: BtDeviceItem) {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            _connectionState.value = BtConnectionState.Error("Bluetooth permission required to pair")
            return
        }
        try {
            stopDiscoveryScan()
            val remoteDevice = bluetoothAdapter.getRemoteDevice(deviceItem.address)
            if (remoteDevice.bondState == BluetoothDevice.BOND_BONDED) {
                connectToDevice(deviceItem.copy(isPaired = true))
            } else {
                _connectionState.value = BtConnectionState.Pairing(deviceItem.name, deviceItem.address)
                val bondInitiated = remoteDevice.createBond()
                if (!bondInitiated) {
                    connectToDevice(deviceItem)
                }
            }
        } catch (e: Exception) {
            _connectionState.value = BtConnectionState.Error("Pairing failed: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceItem: BtDeviceItem) {
        if (!hasBluetoothPermissions()) {
            _connectionState.value = BtConnectionState.Error("Bluetooth permission required")
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BtConnectionState.Error("Bluetooth is turned off")
            return
        }

        scope.launch {
            stopDiscoveryScan()
            disconnectInternal()
            _connectionState.value = BtConnectionState.Connecting(deviceItem.name, deviceItem.address)

            try {
                val device = bluetoothAdapter.getRemoteDevice(deviceItem.address)
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()

                onSocketConnected(socket, deviceItem)
            } catch (e: Exception) {
                try {
                    val device = bluetoothAdapter.getRemoteDevice(deviceItem.address)
                    val fallbackMethod = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    val fallbackSocket = fallbackMethod.invoke(device, 1) as BluetoothSocket
                    fallbackSocket.connect()

                    onSocketConnected(fallbackSocket, deviceItem)
                } catch (_: Exception) {
                    disconnectInternal()
                    _connectionState.value = BtConnectionState.Error(
                        "Unable to connect to ${deviceItem.name}. Make sure the Bluetooth device is powered on."
                    )
                }
            }
        }
    }

    private fun onSocketConnected(socket: BluetoothSocket, deviceItem: BtDeviceItem) {
        activeSocket = socket
        outputStream = socket.outputStream
        lastConnectedDevice = deviceItem.copy(isPaired = true)
        _connectionState.value = BtConnectionState.Connected(deviceItem.name, deviceItem.address)
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
            } catch (_: IOException) {}
        }
    }

    fun disconnect() {
        scope.launch {
            lastConnectedDevice = null
            disconnectInternal()
            _connectionState.value = BtConnectionState.Disconnected
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

    fun sendStartCommand() {
        sendLineToBt(wireLine = "START", displayLabel = "START\\n")
    }

    fun sendStopCommand() {
        sendLineToBt(wireLine = "STOP", displayLabel = "STOP\\n")
    }

    fun sendRpmCommand(rpm: Int) {
        sendLineToBt(wireLine = "RPM=$rpm", displayLabel = "RPM=$rpm\\n")
    }

    fun sendRailCommand(rail: Int) {
        sendLineToBt(wireLine = "RAIL=$rail", displayLabel = "RAIL=$rail\\n")
    }

    fun sendEctCommand(ect: Int) {
        sendLineToBt(wireLine = "ECT=$ect", displayLabel = "ECT=$ect\\n")
    }

    fun sendSpeedCommand(speed: Int) {
        sendLineToBt(wireLine = "SPEED=$speed", displayLabel = "SPEED=$speed\\n")
    }

    fun sendAccelCommand(accel: Int) {
        sendLineToBt(wireLine = "ACCEL=$accel", displayLabel = "ACCEL=$accel\\n")
    }

    fun sendMapCommand(mapKpa: Int) {
        sendLineToBt(wireLine = "MAP=$mapKpa", displayLabel = "MAP=$mapKpa\\n")
    }

    fun sendMafCommand(mafGs: Int) {
        sendLineToBt(wireLine = "MAF=$mafGs", displayLabel = "MAF=$mafGs\\n")
    }

    fun sendCamSyncCommand(enabled: Boolean) {
        val v = if (enabled) 1 else 0
        sendLineToBt(wireLine = "CAM=$v", displayLabel = "CAM=$v\\n")
    }

    fun sendInjectorCommand(enabled: Boolean) {
        val v = if (enabled) 1 else 0
        sendLineToBt(wireLine = "INJ=$v", displayLabel = "INJ=$v\\n")
    }

    fun sendTestPing() {
        sendLineToBt(wireLine = "PING", displayLabel = "PING\\n")
    }

    fun sendCustomRawCommand(rawCommand: String) {
        val cleaned = rawCommand.trim()
        if (cleaned.isEmpty()) return
        sendLineToBt(wireLine = cleaned, displayLabel = "$cleaned\\n")
    }

    private fun sendLineToBt(wireLine: String, displayLabel: String) {
        val payload = "$wireLine\n"
        scope.launch {
            val stream = outputStream
            if (stream != null && _connectionState.value is BtConnectionState.Connected) {
                try {
                    withContext(Dispatchers.IO) {
                        stream.write(payload.toByteArray(Charsets.US_ASCII))
                        stream.flush()
                    }
                    _packetsSentCount.value += 1
                    appendLog("TX -> $displayLabel")
                } catch (e: IOException) {
                    disconnectInternal()
                    _connectionState.value = BtConnectionState.Error("Bluetooth link lost")
                }
            }
        }
    }

    fun sendEcuFrame(
        internalCrankPattern: String,
        rpm: Int,
        railBar: Int,
        ectCelsius: Int,
        speedKmh: Int,
        accelPercent: Int,
        boostMapKpa: Int,
        mafGramsSec: Int,
        camSyncEnabled: Boolean,
        injectorPulseEnabled: Boolean,
        isRunning: Boolean
    ) {
        val mode = _packetMode.value
        if (mode == SerialPacketMode.KeyValueCommands) {
            val batchPayload = buildString {
                append("RPM=$rpm\n")
                append("RAIL=$railBar\n")
                append("ECT=$ectCelsius\n")
                append("SPEED=$speedKmh\n")
                append("ACCEL=$accelPercent\n")
                append("MAP=$boostMapKpa\n")
                append("MAF=$mafGramsSec\n")
                append(if (isRunning) "START\n" else "STOP\n")
            }
            scope.launch {
                val stream = outputStream
                if (stream != null && _connectionState.value is BtConnectionState.Connected) {
                    try {
                        withContext(Dispatchers.IO) {
                            stream.write(batchPayload.toByteArray(Charsets.US_ASCII))
                            stream.flush()
                        }
                        _packetsSentCount.value += 1
                    } catch (_: IOException) {
                        disconnectInternal()
                        _connectionState.value = BtConnectionState.Error("Bluetooth link lost")
                    }
                }
            }
            return
        }

        val runFlag = if (isRunning) 1 else 0
        val camFlag = if (camSyncEnabled) 1 else 0
        val injFlag = if (injectorPulseEnabled) 1 else 0

        val wirePacket = when (mode) {
            SerialPacketMode.Extended11Field ->
                "\$$internalCrankPattern,$rpm,$railBar,$ectCelsius,$speedKmh,$accelPercent,$boostMapKpa,$mafGramsSec,$camFlag,$injFlag,$runFlag\n"
            SerialPacketMode.Standard6Field ->
                "\$$internalCrankPattern,$rpm,$accelPercent,$railBar,$ectCelsius,$runFlag\n"
            SerialPacketMode.KeyValueCommands -> ""
        }

        sendLineToBt(wireLine = wirePacket.trimEnd('\n'), displayLabel = wirePacket.trim())
    }

    fun clearLogs() {
        _txLog.value = emptyList()
    }

    private fun appendLog(entry: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val current = _txLog.value.toMutableList()
        current.add(0, "[$timestamp] $entry")
        if (current.size > 45) {
            current.removeAt(current.lastIndex)
        }
        _txLog.value = current
    }
}
