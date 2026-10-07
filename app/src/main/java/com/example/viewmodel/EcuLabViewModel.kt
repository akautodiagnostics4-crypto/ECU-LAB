package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.bluetooth.BtConnectionState
import com.example.bluetooth.BtDeviceItem
import com.example.bluetooth.Hc05BluetoothController
import com.example.bluetooth.SerialPacketMode
import com.example.data.EcuLabDatabase
import com.example.data.EcuPresetEntity
import com.example.data.EcuPresetRepository
import com.example.data.VehicleCatalog
import com.example.data.VehicleModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BootStage {
    LogoSplash,
    BootLoading,
    Ready
}

enum class EcuLabTab {
    Control,
    Vehicles,
    Saved,
    Device
}

enum class AccentColorTheme(val label: String) {
    CyberBlue("Electric Blue"),
    EmeraldGreen("Diagnostic Green"),
    RacingAmber("Turbo Amber"),
    CrimsonRed("Motorsport Red")
}

data class EcuControlUiState(
    val bootStage: BootStage = BootStage.LogoSplash,
    val activeTab: EcuLabTab = EcuLabTab.Vehicles, // Opens directly to All Vehicle List after boot!
    val selectedVehicle: VehicleModel = VehicleCatalog.allVehicles.first(),
    val selectedBrandFilter: String = "ALL",
    val searchQuery: String = "",
    val isSignalRunning: Boolean = false,
    val rpm: Int = 800,                    // Range: 200 to 2500 rpm
    val railPressure: Int = 300,           // Range: 100 to 1000 bar
    val ectTemp: Int = 90,                 // Range: 0 to 200 °C
    val vehicleSpeed: Int = 0,             // Range: 0 to 220 km/h
    val accelerator: Int = 0,              // Range: 0 to 100 %
    val boostMapKpa: Int = 105,            // Range: 100 to 300 kPa (Boost / MAP sensor)
    val mafGramsSec: Int = 18,             // Range: 0 to 500 g/s (MAF Air Flow sensor)
    val camSyncEnabled: Boolean = true,    // Camshaft CMP Sync pulse output ON/OFF
    val injectorPulseEnabled: Boolean = true, // Injector Load Feedback pulse ON/OFF
    val accentTheme: AccentColorTheme = AccentColorTheme.CyberBlue,
    val showSaveDialog: Boolean = false,
    val showArduinoSketchModal: Boolean = false,
    val showBtQuickSheet: Boolean = false,
    val statusBannerMessage: String? = null
)

class EcuLabViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val RPM_MIN = 200
        const val RPM_MAX = 2500

        const val RAIL_MIN = 100
        const val RAIL_MAX = 1000

        const val ECT_MIN = 0
        const val ECT_MAX = 200

        const val SPEED_MIN = 0
        const val SPEED_MAX = 220

        const val ACCEL_MIN = 0
        const val ACCEL_MAX = 100

        const val MAP_MIN = 100
        const val MAP_MAX = 300

        const val MAF_MIN = 0
        const val MAF_MAX = 500
    }

    private val database: EcuLabDatabase = Room.databaseBuilder(
        application,
        EcuLabDatabase::class.java,
        "ecu_lab_db"
    ).fallbackToDestructiveMigration(false).build()

    private val repository = EcuPresetRepository(database.ecuPresetDao())
    val bluetoothController = Hc05BluetoothController(application)

    private val _uiState = MutableStateFlow(EcuControlUiState())
    val uiState: StateFlow<EcuControlUiState> = _uiState.asStateFlow()

    val savedPresets: StateFlow<List<EcuPresetEntity>> = repository.allPresets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val btConnectionState: StateFlow<BtConnectionState> = bluetoothController.connectionState
    val pairedDevices: StateFlow<List<BtDeviceItem>> = bluetoothController.pairedDevices
    val discoveredDevices: StateFlow<List<BtDeviceItem>> = bluetoothController.discoveredDevices
    val isBtScanning: StateFlow<Boolean> = bluetoothController.isScanning
    val autoReconnect: StateFlow<Boolean> = bluetoothController.autoReconnect
    val packetMode: StateFlow<SerialPacketMode> = bluetoothController.packetMode
    val txLog: StateFlow<List<String>> = bluetoothController.txLog
    val packetsSentCount: StateFlow<Int> = bluetoothController.packetsSentCount
    val packetsReceivedCount: StateFlow<Int> = bluetoothController.packetsReceivedCount
    val lastArduinoAck: StateFlow<String?> = bluetoothController.lastArduinoAck

    init {
        runBootSequence()
    }

    private fun runBootSequence() {
        viewModelScope.launch {
            _uiState.update { it.copy(bootStage = BootStage.LogoSplash) }
            delay(3000L) // AFTER 3 SEC
            _uiState.update { it.copy(bootStage = BootStage.BootLoading) }
            delay(2000L) // BOOT LOADING... AFTER 2 SECOND
            _uiState.update {
                it.copy(
                    bootStage = BootStage.Ready,
                    activeTab = EcuLabTab.Vehicles // ALL VEHICLE LIST APPEAR ON THE SCREEN
                )
            }
        }
    }

    fun skipBootForTest() {
        _uiState.update {
            it.copy(
                bootStage = BootStage.Ready,
                activeTab = EcuLabTab.Vehicles
            )
        }
    }

    fun selectTab(tab: EcuLabTab) {
        _uiState.update { it.copy(activeTab = tab) }
        if (tab == EcuLabTab.Device) {
            bluetoothController.refreshPairedDevices()
        }
    }

    fun setShowBtQuickSheet(show: Boolean) {
        if (show) {
            bluetoothController.refreshPairedDevices()
        }
        _uiState.update { it.copy(showBtQuickSheet = show) }
    }

    fun setAccentTheme(theme: AccentColorTheme) {
        _uiState.update { it.copy(accentTheme = theme) }
    }

    fun selectVehicle(vehicle: VehicleModel) {
        val safeRpm = vehicle.defaultRpm.coerceIn(RPM_MIN, RPM_MAX)
        val safeRail = vehicle.defaultRail.coerceIn(RAIL_MIN, RAIL_MAX)
        val safeEct = vehicle.defaultEct.coerceIn(ECT_MIN, ECT_MAX)
        val safeSpeed = vehicle.defaultSpeed.coerceIn(SPEED_MIN, SPEED_MAX)
        val safeAccel = vehicle.defaultAccel.coerceIn(ACCEL_MIN, ACCEL_MAX)
        val safeMap = vehicle.defaultBoostMap.coerceIn(MAP_MIN, MAP_MAX)
        val safeMaf = vehicle.defaultMaf.coerceIn(MAF_MIN, MAF_MAX)

        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                rpm = safeRpm,
                railPressure = safeRail,
                ectTemp = safeEct,
                vehicleSpeed = safeSpeed,
                accelerator = safeAccel,
                boostMapKpa = safeMap,
                mafGramsSec = safeMaf,
                camSyncEnabled = vehicle.defaultCamSync,
                injectorPulseEnabled = vehicle.defaultInjectorPulse,
                activeTab = EcuLabTab.Control,
                statusBannerMessage = "${vehicle.brand} ${vehicle.modelName} loaded"
            )
        }
        transmitCurrentFrame()
    }

    /**
     * Quick Bench Test Scenarios (Cranking, Idle, Cruise, Full Load, Overheat Test)
     */
    fun applyQuickBenchMode(modeName: String) {
        val v = _uiState.value.selectedVehicle
        when (modeName) {
            "CRANKING" -> _uiState.update {
                it.copy(
                    rpm = 250,
                    railPressure = 220,
                    ectTemp = 45,
                    vehicleSpeed = 0,
                    accelerator = 0,
                    boostMapKpa = 100,
                    mafGramsSec = 8,
                    statusBannerMessage = "Applied Cranking Mode (250 RPM)"
                )
            }
            "IDLE" -> _uiState.update {
                it.copy(
                    rpm = v.defaultRpm.coerceIn(RPM_MIN, RPM_MAX),
                    railPressure = v.defaultRail.coerceIn(RAIL_MIN, RAIL_MAX),
                    ectTemp = 88,
                    vehicleSpeed = 0,
                    accelerator = 0,
                    boostMapKpa = v.defaultBoostMap.coerceIn(MAP_MIN, MAP_MAX),
                    mafGramsSec = v.defaultMaf.coerceIn(MAF_MIN, MAF_MAX),
                    statusBannerMessage = "Applied Warm Idle Mode (${v.defaultRpm} RPM)"
                )
            }
            "CRUISE" -> _uiState.update {
                it.copy(
                    rpm = 1650,
                    railPressure = 620,
                    ectTemp = 92,
                    vehicleSpeed = 75,
                    accelerator = 35,
                    boostMapKpa = 165,
                    mafGramsSec = 95,
                    statusBannerMessage = "Applied Highway Cruise Mode (1650 RPM)"
                )
            }
            "FULL_LOAD" -> _uiState.update {
                it.copy(
                    rpm = 2400,
                    railPressure = 950,
                    ectTemp = 98,
                    vehicleSpeed = 120,
                    accelerator = 90,
                    boostMapKpa = 245,
                    mafGramsSec = 260,
                    statusBannerMessage = "Applied Full Load / Boost Test (2400 RPM)"
                )
            }
        }
        transmitCurrentFrame()
    }

    fun setBrandFilter(brand: String) {
        _uiState.update { it.copy(selectedBrandFilter = brand) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateRpm(newRpm: Int) {
        val clamped = newRpm.coerceIn(RPM_MIN, RPM_MAX)
        _uiState.update { it.copy(rpm = clamped) }
        transmitCurrentFrame()
    }

    fun updateRailPressure(newRail: Int) {
        val clamped = newRail.coerceIn(RAIL_MIN, RAIL_MAX)
        _uiState.update { it.copy(railPressure = clamped) }
        transmitCurrentFrame()
    }

    fun updateEctTemp(newEct: Int) {
        val clamped = newEct.coerceIn(ECT_MIN, ECT_MAX)
        _uiState.update { it.copy(ectTemp = clamped) }
        transmitCurrentFrame()
    }

    fun updateVehicleSpeed(newSpeed: Int) {
        val clamped = newSpeed.coerceIn(SPEED_MIN, SPEED_MAX)
        _uiState.update { it.copy(vehicleSpeed = clamped) }
        transmitCurrentFrame()
    }

    fun updateAccelerator(newAccel: Int) {
        val clamped = newAccel.coerceIn(ACCEL_MIN, ACCEL_MAX)
        _uiState.update { it.copy(accelerator = clamped) }
        transmitCurrentFrame()
    }

    fun updateBoostMap(newMapKpa: Int) {
        val clamped = newMapKpa.coerceIn(MAP_MIN, MAP_MAX)
        _uiState.update { it.copy(boostMapKpa = clamped) }
        transmitCurrentFrame()
    }

    fun updateMaf(newMaf: Int) {
        val clamped = newMaf.coerceIn(MAF_MIN, MAF_MAX)
        _uiState.update { it.copy(mafGramsSec = clamped) }
        transmitCurrentFrame()
    }

    fun toggleCamSync(enabled: Boolean) {
        _uiState.update {
            it.copy(
                camSyncEnabled = enabled,
                statusBannerMessage = if (enabled) "CMP Camshaft Sync Pulse ENABLED" else "CMP Camshaft Sync Pulse DISABLED"
            )
        }
        transmitCurrentFrame()
    }

    fun toggleInjectorPulse(enabled: Boolean) {
        _uiState.update {
            it.copy(
                injectorPulseEnabled = enabled,
                statusBannerMessage = if (enabled) "Injector Load Pulse ENABLED" else "Injector Load Pulse DISABLED"
            )
        }
        transmitCurrentFrame()
    }

    fun toggleSignalGeneration() {
        _uiState.update { state ->
            val nextRunning = !state.isSignalRunning
            state.copy(
                isSignalRunning = nextRunning,
                statusBannerMessage = if (nextRunning) "ECU Signal Generation STARTED" else "ECU Signal Generation STOPPED"
            )
        }
        transmitCurrentFrame()
    }

    private fun transmitCurrentFrame() {
        val s = _uiState.value
        bluetoothController.sendEcuFrame(
            internalCrankPattern = s.selectedVehicle.crankPattern,
            rpm = s.rpm,
            railBar = s.railPressure,
            ectCelsius = s.ectTemp,
            speedKmh = s.vehicleSpeed,
            accelPercent = s.accelerator,
            boostMapKpa = s.boostMapKpa,
            mafGramsSec = s.mafGramsSec,
            camSyncEnabled = s.camSyncEnabled,
            injectorPulseEnabled = s.injectorPulseEnabled,
            isRunning = s.isSignalRunning
        )
    }

    fun openSavePresetDialog() {
        _uiState.update { it.copy(showSaveDialog = true) }
    }

    fun dismissSavePresetDialog() {
        _uiState.update { it.copy(showSaveDialog = false) }
    }

    fun saveCurrentPreset(customName: String) {
        val s = _uiState.value
        val finalName = customName.trim().ifEmpty {
            "${s.selectedVehicle.brand} ${s.selectedVehicle.modelName} @ ${s.rpm} RPM"
        }
        viewModelScope.launch {
            repository.savePreset(
                EcuPresetEntity(
                    presetName = finalName,
                    vehicleId = s.selectedVehicle.id,
                    brand = s.selectedVehicle.brand,
                    modelName = s.selectedVehicle.modelName,
                    rpm = s.rpm,
                    railPressure = s.railPressure,
                    ectTemp = s.ectTemp,
                    vehicleSpeed = s.vehicleSpeed,
                    accelerator = s.accelerator,
                    boostMapKpa = s.boostMapKpa,
                    mafGramsSec = s.mafGramsSec,
                    camSyncEnabled = s.camSyncEnabled,
                    injectorPulseEnabled = s.injectorPulseEnabled
                )
            )
            _uiState.update {
                it.copy(
                    showSaveDialog = false,
                    statusBannerMessage = "Preset '$finalName' saved"
                )
            }
        }
    }

    fun loadPreset(preset: EcuPresetEntity) {
        val vehicle = VehicleCatalog.findById(preset.vehicleId)
        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                rpm = preset.rpm.coerceIn(RPM_MIN, RPM_MAX),
                railPressure = preset.railPressure.coerceIn(RAIL_MIN, RAIL_MAX),
                ectTemp = preset.ectTemp.coerceIn(ECT_MIN, ECT_MAX),
                vehicleSpeed = preset.vehicleSpeed.coerceIn(SPEED_MIN, SPEED_MAX),
                accelerator = preset.accelerator.coerceIn(ACCEL_MIN, ACCEL_MAX),
                boostMapKpa = preset.boostMapKpa.coerceIn(MAP_MIN, MAP_MAX),
                mafGramsSec = preset.mafGramsSec.coerceIn(MAF_MIN, MAF_MAX),
                camSyncEnabled = preset.camSyncEnabled,
                injectorPulseEnabled = preset.injectorPulseEnabled,
                activeTab = EcuLabTab.Control,
                statusBannerMessage = "Loaded preset: ${preset.presetName}"
            )
        }
        transmitCurrentFrame()
    }

    fun deletePreset(id: Int) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }

    fun setShowArduinoSketchModal(show: Boolean) {
        _uiState.update { it.copy(showArduinoSketchModal = show) }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun refreshBluetoothDevices() {
        bluetoothController.refreshPairedDevices()
    }

    fun startBtDiscoveryScan() {
        bluetoothController.startDiscoveryScan()
    }

    fun stopBtDiscoveryScan() {
        bluetoothController.stopDiscoveryScan()
    }

    fun quickConnectHc05() {
        bluetoothController.quickConnectHc05()
    }

    fun pairAndConnectHc05(device: BtDeviceItem) {
        bluetoothController.pairAndConnectDevice(device)
    }

    fun connectHc05(device: BtDeviceItem) {
        bluetoothController.connectToDevice(device)
    }

    fun disconnectHc05() {
        bluetoothController.disconnect()
    }

    fun setAutoReconnect(enabled: Boolean) {
        bluetoothController.setAutoReconnect(enabled)
    }

    fun setSerialPacketMode(mode: SerialPacketMode) {
        bluetoothController.setSerialPacketMode(mode)
        transmitCurrentFrame()
    }

    fun sendCustomSerialCommand(cmd: String) {
        bluetoothController.sendCustomRawCommand(cmd)
    }

    fun sendTestPing() {
        bluetoothController.sendTestPing()
        transmitCurrentFrame()
    }

    fun clearBtLogs() {
        bluetoothController.clearLogs()
    }
}
