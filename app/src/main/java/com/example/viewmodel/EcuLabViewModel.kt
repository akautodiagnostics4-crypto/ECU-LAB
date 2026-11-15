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
import com.example.data.ObdFaultCatalog
import com.example.data.ObdFaultCode
import com.example.data.StimulableSensor
import com.example.data.StimulationHistoryEntity
import com.example.data.StimulationHistoryRepository
import com.example.data.VehicleCatalog
import com.example.data.VehicleCategoryType
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

enum class AppRoute {
    Home,
    EcuCategories,
    ComingSoonBrands,
    LcvBrands,
    LcvVehicles,
    EcuCrankingSetup,
    EcuRunPage,
    SensorStimulation,
    BluetoothDevices,
    SavedPresets,
    StimulationHistory,
    ContactInfo,
    Settings
}

enum class CrankingModePreset(
    val id: String,
    val title: String,
    val rpm: Int,
    val railBar: Int
) {
    MODE_1("mode1", "MODE 1", 300, 250),
    MODE_2("mode2", "MODE 2", 700, 350),
    MODE_3("mode3", "MODE 3", 1200, 600),
    CUSTOM("custom", "CUSTOM", 800, 350)
}

enum class AccentColorTheme(val label: String) {
    CyberBlue("Electric Blue"),
    EmeraldGreen("Diagnostic Green"),
    RacingAmber("Turbo Amber"),
    CrimsonRed("Motorsport Red")
}

data class EcuControlUiState(
    val bootStage: BootStage = BootStage.LogoSplash,
    val currentRoute: AppRoute = AppRoute.Home,
    val selectedCategory: VehicleCategoryType = VehicleCategoryType.LCV,
    val selectedLcvBrand: String = "TATA",
    val selectedVehicle: VehicleModel = VehicleCatalog.allVehicles.first(),
    val selectedCrankingMode: CrankingModePreset = CrankingModePreset.MODE_1,
    val ecuPickedSensors: Set<StimulableSensor> = setOf(StimulableSensor.RAIL_PRESSURE),
    val sensorStimulationTicked: Set<StimulableSensor> = setOf(
        StimulableSensor.RAIL_PRESSURE,
        StimulableSensor.COOLANT_TEMP
    ),
    val isSignalRunning: Boolean = false,
    val isSensorStimulationRunning: Boolean = false,
    val rpm: Int = CrankingModePreset.MODE_1.rpm,           // 200 to 2500 rpm (0 on reset)
    val railPressure: Int = CrankingModePreset.MODE_1.railBar, // 0 to 1200 bar
    val ectTemp: Int = 88,                                  // 0 to 125 °C
    val vehicleSpeed: Int = 40,                             // 0 to 220 km/h
    val accelerator: Int = 0,                               // 0 to 100 %
    val boostMapKpa: Int = 130,                             // 100 to 300 kPa
    val mafGramsSec: Int = 35,                              // 0 to 500 g/s
    val camSyncEnabled: Boolean = true,
    val injectorPulseEnabled: Boolean = true,
    val accentTheme: AccentColorTheme = AccentColorTheme.CyberBlue,
    val activeObdFaults: Set<ObdFaultCode> = emptySet(),
    val obdFaultEventLog: List<String> = emptyList(),
    val showObdFaultSheet: Boolean = false,
    val showSaveDialog: Boolean = false,
    val showBtQuickSheet: Boolean = false,
    val showBtConnectFirstDialog: Boolean = false,
    val statusBannerMessage: String? = null
)

class EcuLabViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val RPM_MIN = 200
        const val RPM_MAX = 2500

        const val RAIL_MIN = 0
        const val RAIL_MAX = 1200

        const val ECT_MIN = 0
        const val ECT_MAX = 125

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
    ).fallbackToDestructiveMigration(true).build()

    private val repository = EcuPresetRepository(database.ecuPresetDao())
    private val historyRepository = StimulationHistoryRepository(database.stimulationHistoryDao())
    val bluetoothController = Hc05BluetoothController(application)

    private val _uiState = MutableStateFlow(EcuControlUiState())
    val uiState: StateFlow<EcuControlUiState> = _uiState.asStateFlow()

    val savedPresets: StateFlow<List<EcuPresetEntity>> = repository.allPresets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stimulationHistory: StateFlow<List<StimulationHistoryEntity>> = historyRepository.allHistory.stateIn(
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

    private val routeHistory = ArrayDeque<AppRoute>()

    init {
        runBootSequence()
        observeBluetoothDisconnectSafety()
    }

    private fun runBootSequence() {
        viewModelScope.launch {
            _uiState.update { it.copy(bootStage = BootStage.LogoSplash) }
            delay(2000L)
            _uiState.update { it.copy(bootStage = BootStage.BootLoading) }
            bluetoothController.attemptAutoConnect()
            delay(1500L)
            _uiState.update {
                it.copy(
                    bootStage = BootStage.Ready,
                    currentRoute = AppRoute.Home
                )
            }
            bluetoothController.attemptAutoConnect()
        }
    }

    /**
     * Strict safety requirement:
     * "if Bluetooth disconnect cranking and stimulation should be stopped"
     */
    private fun observeBluetoothDisconnectSafety() {
        viewModelScope.launch {
            var wasConnected = false
            bluetoothController.connectionState.collect { btState ->
                val isNowConnected = btState is BtConnectionState.Connected
                if (wasConnected && !isNowConnected) {
                    val hadRunning = _uiState.value.isSignalRunning || _uiState.value.isSensorStimulationRunning
                    if (hadRunning) {
                        _uiState.update {
                            it.copy(
                                isSignalRunning = false,
                                isSensorStimulationRunning = false,
                                statusBannerMessage = "Bluetooth disconnected — Cranking and stimulation stopped"
                            )
                        }
                    }
                }
                wasConnected = isNowConnected
            }
        }
    }

    fun tryBluetoothAutoConnect() {
        bluetoothController.attemptAutoConnect(forceRetry = true)
    }

    fun navigateTo(route: AppRoute) {
        val current = _uiState.value.currentRoute
        if (current != route) {
            routeHistory.addLast(current)
            _uiState.update { it.copy(currentRoute = route) }
            if (route == AppRoute.BluetoothDevices || route == AppRoute.Home) {
                bluetoothController.refreshPairedDevices()
            }
        }
    }

    fun navigateBack(): Boolean {
        return if (routeHistory.isNotEmpty()) {
            val prev = routeHistory.removeLast()
            _uiState.update { it.copy(currentRoute = prev) }
            true
        } else if (_uiState.value.currentRoute != AppRoute.Home) {
            _uiState.update { it.copy(currentRoute = AppRoute.Home) }
            true
        } else {
            false
        }
    }

    fun navigateHome() {
        routeHistory.clear()
        _uiState.update { it.copy(currentRoute = AppRoute.Home) }
    }

    fun openEcuStimulation() {
        navigateTo(AppRoute.EcuCategories)
    }

    fun openSensorStimulation() {
        // Ensure ECU cranking is stopped when entering Sensor-only stimulation
        if (_uiState.value.isSignalRunning) {
            stopEcuStimulationInternal()
        }
        navigateTo(AppRoute.SensorStimulation)
    }

    fun selectVehicleCategory(category: VehicleCategoryType) {
        _uiState.update { it.copy(selectedCategory = category) }
        if (category.isComingSoon) {
            navigateTo(AppRoute.ComingSoonBrands)
        } else {
            navigateTo(AppRoute.LcvBrands)
        }
    }

    fun selectLcvBrand(brand: String) {
        _uiState.update { it.copy(selectedLcvBrand = brand) }
        navigateTo(AppRoute.LcvVehicles)
    }

    fun selectLcvVehicle(vehicle: VehicleModel) {
        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                selectedCrankingMode = CrankingModePreset.MODE_1,
                rpm = CrankingModePreset.MODE_1.rpm,
                railPressure = CrankingModePreset.MODE_1.railBar,
                ectTemp = vehicle.defaultEct.coerceIn(ECT_MIN, ECT_MAX),
                boostMapKpa = vehicle.defaultBoostMap.coerceIn(MAP_MIN, MAP_MAX),
                mafGramsSec = vehicle.defaultMaf.coerceIn(MAF_MIN, MAF_MAX),
                camSyncEnabled = vehicle.defaultCamSync,
                injectorPulseEnabled = vehicle.defaultInjectorPulse,
                activeObdFaults = emptySet()
            )
        }
        navigateTo(AppRoute.EcuCrankingSetup)
    }

    fun selectCrankingMode(mode: CrankingModePreset) {
        _uiState.update {
            if (mode == CrankingModePreset.CUSTOM) {
                it.copy(selectedCrankingMode = CrankingModePreset.CUSTOM)
            } else {
                it.copy(
                    selectedCrankingMode = mode,
                    rpm = mode.rpm,
                    railPressure = mode.railBar
                )
            }
        }
        if (_uiState.value.isSignalRunning) {
            transmitEcuFrame()
        }
    }

    fun toggleEcuPickedSensor(sensor: StimulableSensor) {
        val current = _uiState.value.ecuPickedSensors.toMutableSet()
        if (sensor in current) {
            current.remove(sensor)
        } else {
            current.add(sensor)
        }
        _uiState.update { it.copy(ecuPickedSensors = current) }
    }

    fun proceedToEcuStartPage() {
        navigateTo(AppRoute.EcuRunPage)
    }

    fun setShowObdFaultSheet(show: Boolean) {
        _uiState.update { it.copy(showObdFaultSheet = show) }
    }

    /**
     * Injects or clears an individual OBD-II sensor/cranking fault inside ECU Stimulation mode.
     * When injected, applies the corresponding simulated sensor failure value and notifies connected hardware.
     */
    fun toggleObdFault(fault: ObdFaultCode) {
        val current = _uiState.value
        val nextFaults = current.activeObdFaults.toMutableSet()
        if (fault in nextFaults) {
            nextFaults.remove(fault)
            val restored = restoreBaselineForClearedFault(current, fault)
            val logLine = "CLEARED ${fault.code} (${fault.title})"
            _uiState.update {
                restored.copy(
                    activeObdFaults = nextFaults,
                    obdFaultEventLog = (listOf(logLine) + it.obdFaultEventLog).take(12),
                    statusBannerMessage = "DTC Cleared: ${fault.code}"
                )
            }
            if (nextFaults.isEmpty() && bluetoothController.connectionState.value is BtConnectionState.Connected) {
                bluetoothController.sendObdFaultClearCommand()
            }
        } else {
            nextFaults.add(fault)
            val faultedState = applyFaultConditionsToState(current, fault)
            val logLine = "INJECTED ${fault.code} — ${fault.faultConditionSummary}"
            _uiState.update {
                faultedState.copy(
                    activeObdFaults = nextFaults,
                    obdFaultEventLog = (listOf(logLine) + it.obdFaultEventLog).take(12),
                    statusBannerMessage = "Fault Injected: ${fault.code} (MIL ON)"
                )
            }
            if (bluetoothController.connectionState.value is BtConnectionState.Connected) {
                bluetoothController.sendObdFaultInjectCommand(fault.code)
            }
        }

        if (_uiState.value.isSignalRunning && bluetoothController.connectionState.value is BtConnectionState.Connected) {
            transmitEcuFrame()
        }
    }

    fun generateRandomObdFault() {
        val candidate = ObdFaultCatalog.pickRandomCandidate(_uiState.value.activeObdFaults)
        if (candidate in _uiState.value.activeObdFaults) {
            val faultedState = applyFaultConditionsToState(_uiState.value, candidate)
            _uiState.update {
                faultedState.copy(
                    statusBannerMessage = "Fault Active: ${candidate.code} — ${candidate.title}"
                )
            }
            if (_uiState.value.isSignalRunning && bluetoothController.connectionState.value is BtConnectionState.Connected) {
                transmitEcuFrame()
            }
        } else {
            toggleObdFault(candidate)
        }
    }

    fun clearAllObdFaults() {
        val current = _uiState.value
        val v = current.selectedVehicle
        val mode = current.selectedCrankingMode
        val restoredRpm = if (mode == CrankingModePreset.CUSTOM) v.defaultRpm else mode.rpm
        val restoredRail = if (mode == CrankingModePreset.CUSTOM) v.defaultRail else mode.railBar

        _uiState.update {
            it.copy(
                activeObdFaults = emptySet(),
                rpm = restoredRpm.coerceIn(RPM_MIN, RPM_MAX),
                railPressure = restoredRail.coerceIn(RAIL_MIN, RAIL_MAX),
                ectTemp = v.defaultEct.coerceIn(ECT_MIN, ECT_MAX),
                vehicleSpeed = v.defaultSpeed.coerceIn(SPEED_MIN, SPEED_MAX),
                boostMapKpa = v.defaultBoostMap.coerceIn(MAP_MIN, MAP_MAX),
                mafGramsSec = v.defaultMaf.coerceIn(MAF_MIN, MAF_MAX),
                camSyncEnabled = v.defaultCamSync,
                obdFaultEventLog = (listOf("ALL DTC CLEARED — MIL OFF") + it.obdFaultEventLog).take(12),
                statusBannerMessage = "All OBD-II DTCs Cleared — Baseline Restored"
            )
        }
        if (bluetoothController.connectionState.value is BtConnectionState.Connected) {
            bluetoothController.sendObdFaultClearCommand()
            if (_uiState.value.isSignalRunning) {
                transmitEcuFrame()
            }
        }
    }

    private fun applyFaultConditionsToState(
        state: EcuControlUiState,
        fault: ObdFaultCode
    ): EcuControlUiState {
        val picked = state.ecuPickedSensors.toMutableSet()
        fault.targetSensor?.let { picked.add(it) }

        val nextRpm = fault.simulatedRpmOverride?.coerceIn(RPM_MIN, RPM_MAX) ?: state.rpm
        val nextCamSync = if (fault.disablesCamSync) false else state.camSyncEnabled

        val sensorVal = fault.simulatedSensorValue
        return when (fault.targetSensor) {
            StimulableSensor.RAIL_PRESSURE -> state.copy(
                ecuPickedSensors = picked,
                railPressure = (sensorVal ?: state.railPressure).coerceIn(RAIL_MIN, RAIL_MAX),
                selectedCrankingMode = CrankingModePreset.CUSTOM
            )
            StimulableSensor.COOLANT_TEMP -> state.copy(
                ecuPickedSensors = picked,
                ectTemp = (sensorVal ?: state.ectTemp).coerceIn(ECT_MIN, ECT_MAX)
            )
            StimulableSensor.VEHICLE_SPEED -> state.copy(
                ecuPickedSensors = picked,
                vehicleSpeed = (sensorVal ?: state.vehicleSpeed).coerceIn(SPEED_MIN, SPEED_MAX)
            )
            StimulableSensor.BOOST_PRESSURE -> state.copy(
                ecuPickedSensors = picked,
                boostMapKpa = (sensorVal ?: state.boostMapKpa).coerceIn(MAP_MIN, MAP_MAX)
            )
            StimulableSensor.AIR_MASS -> state.copy(
                ecuPickedSensors = picked,
                mafGramsSec = (sensorVal ?: state.mafGramsSec).coerceIn(MAF_MIN, MAF_MAX)
            )
            null -> state.copy(
                ecuPickedSensors = picked,
                rpm = nextRpm,
                camSyncEnabled = nextCamSync,
                selectedCrankingMode = if (fault.simulatedRpmOverride != null) CrankingModePreset.CUSTOM else state.selectedCrankingMode
            )
        }
    }

    private fun restoreBaselineForClearedFault(
        state: EcuControlUiState,
        fault: ObdFaultCode
    ): EcuControlUiState {
        val v = state.selectedVehicle
        return when (fault.targetSensor) {
            StimulableSensor.RAIL_PRESSURE -> state.copy(
                railPressure = v.defaultRail.coerceIn(RAIL_MIN, RAIL_MAX)
            )
            StimulableSensor.COOLANT_TEMP -> state.copy(
                ectTemp = v.defaultEct.coerceIn(ECT_MIN, ECT_MAX)
            )
            StimulableSensor.VEHICLE_SPEED -> state.copy(
                vehicleSpeed = v.defaultSpeed.coerceIn(SPEED_MIN, SPEED_MAX)
            )
            StimulableSensor.BOOST_PRESSURE -> state.copy(
                boostMapKpa = v.defaultBoostMap.coerceIn(MAP_MIN, MAP_MAX)
            )
            StimulableSensor.AIR_MASS -> state.copy(
                mafGramsSec = v.defaultMaf.coerceIn(MAF_MIN, MAF_MAX)
            )
            null -> state.copy(
                rpm = if (fault.simulatedRpmOverride != null) v.defaultRpm.coerceIn(RPM_MIN, RPM_MAX) else state.rpm,
                camSyncEnabled = if (fault.disablesCamSync) v.defaultCamSync else state.camSyncEnabled
            )
        }
    }

    /**
     * Toggle Tick button in Sensor Stimulation category (allows selecting any number of sensors).
     */
    fun toggleSensorStimulationTick(sensor: StimulableSensor) {
        val current = _uiState.value.sensorStimulationTicked.toMutableSet()
        if (sensor in current) {
            current.remove(sensor)
            _uiState.update { it.copy(sensorStimulationTicked = current) }
            if (_uiState.value.isSensorStimulationRunning && current.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isSensorStimulationRunning = false,
                        statusBannerMessage = "Select at least 1 sensor to stimulate"
                    )
                }
                bluetoothController.sendStopCommand()
            } else if (_uiState.value.isSensorStimulationRunning) {
                transmitSensorOnlyFrame()
            }
        } else {
            current.add(sensor)
            _uiState.update { it.copy(sensorStimulationTicked = current) }
            if (_uiState.value.isSensorStimulationRunning) {
                transmitSensorOnlyFrame()
            }
        }
    }

    fun updateSensorValue(sensor: StimulableSensor, newVal: Int) {
        when (sensor) {
            StimulableSensor.RAIL_PRESSURE -> updateRailPressure(newVal)
            StimulableSensor.COOLANT_TEMP -> updateEctTemp(newVal)
            StimulableSensor.VEHICLE_SPEED -> updateVehicleSpeed(newVal)
            StimulableSensor.BOOST_PRESSURE -> updateBoostMap(newVal)
            StimulableSensor.AIR_MASS -> updateMaf(newVal)
        }
    }

    fun getSensorValue(sensor: StimulableSensor): Int {
        val s = _uiState.value
        return when (sensor) {
            StimulableSensor.RAIL_PRESSURE -> s.railPressure
            StimulableSensor.COOLANT_TEMP -> s.ectTemp
            StimulableSensor.VEHICLE_SPEED -> s.vehicleSpeed
            StimulableSensor.BOOST_PRESSURE -> s.boostMapKpa
            StimulableSensor.AIR_MASS -> s.mafGramsSec
        }
    }

    fun updateRpm(newRpm: Int) {
        val clamped = newRpm.coerceIn(RPM_MIN, RPM_MAX)
        _uiState.update {
            it.copy(
                rpm = clamped,
                selectedCrankingMode = CrankingModePreset.CUSTOM
            )
        }
        if (_uiState.value.isSignalRunning) {
            if (bluetoothController.packetMode.value == SerialPacketMode.KeyValueCommands) {
                bluetoothController.sendRpmCommand(clamped)
            } else {
                transmitEcuFrame()
            }
        }
    }

    fun updateRailPressure(newRail: Int) {
        val clamped = newRail.coerceIn(RAIL_MIN, RAIL_MAX)
        _uiState.update {
            it.copy(
                railPressure = clamped,
                selectedCrankingMode = if (it.currentRoute == AppRoute.EcuCrankingSetup) CrankingModePreset.CUSTOM else it.selectedCrankingMode
            )
        }
        if (_uiState.value.isSignalRunning || _uiState.value.isSensorStimulationRunning) {
            if (bluetoothController.packetMode.value == SerialPacketMode.KeyValueCommands) {
                bluetoothController.sendRailCommand(clamped)
            } else if (_uiState.value.isSensorStimulationRunning) {
                transmitSensorOnlyFrame()
            } else {
                transmitEcuFrame()
            }
        }
    }

    fun updateEctTemp(newEct: Int) {
        val clamped = newEct.coerceIn(ECT_MIN, ECT_MAX)
        _uiState.update { it.copy(ectTemp = clamped) }
        if (_uiState.value.isSignalRunning || _uiState.value.isSensorStimulationRunning) {
            if (bluetoothController.packetMode.value == SerialPacketMode.KeyValueCommands) {
                bluetoothController.sendEctCommand(clamped)
            } else if (_uiState.value.isSensorStimulationRunning) {
                transmitSensorOnlyFrame()
            } else {
                transmitEcuFrame()
            }
        }
    }

    fun updateVehicleSpeed(newSpeed: Int) {
        val clamped = newSpeed.coerceIn(SPEED_MIN, SPEED_MAX)
        _uiState.update { it.copy(vehicleSpeed = clamped) }
        if (_uiState.value.isSignalRunning || _uiState.value.isSensorStimulationRunning) {
            if (bluetoothController.packetMode.value == SerialPacketMode.KeyValueCommands) {
                bluetoothController.sendSpeedCommand(clamped)
            } else if (_uiState.value.isSensorStimulationRunning) {
                transmitSensorOnlyFrame()
            } else {
                transmitEcuFrame()
            }
        }
    }

    fun updateBoostMap(newMapKpa: Int) {
        val clamped = newMapKpa.coerceIn(MAP_MIN, MAP_MAX)
        _uiState.update { it.copy(boostMapKpa = clamped) }
        if (_uiState.value.isSignalRunning || _uiState.value.isSensorStimulationRunning) {
            if (bluetoothController.packetMode.value == SerialPacketMode.KeyValueCommands) {
                bluetoothController.sendMapCommand(clamped)
            } else if (_uiState.value.isSensorStimulationRunning) {
                transmitSensorOnlyFrame()
            } else {
                transmitEcuFrame()
            }
        }
    }

    fun updateMaf(newMaf: Int) {
        val clamped = newMaf.coerceIn(MAF_MIN, MAF_MAX)
        _uiState.update { it.copy(mafGramsSec = clamped) }
        if (_uiState.value.isSignalRunning || _uiState.value.isSensorStimulationRunning) {
            if (bluetoothController.packetMode.value == SerialPacketMode.KeyValueCommands) {
                bluetoothController.sendMafCommand(clamped)
            } else if (_uiState.value.isSensorStimulationRunning) {
                transmitSensorOnlyFrame()
            } else {
                transmitEcuFrame()
            }
        }
    }

    fun toggleCamSync(enabled: Boolean) {
        _uiState.update { it.copy(camSyncEnabled = enabled) }
        if (_uiState.value.isSignalRunning) {
            if (bluetoothController.packetMode.value == SerialPacketMode.KeyValueCommands) {
                bluetoothController.sendCamSyncCommand(enabled)
            } else {
                transmitEcuFrame()
            }
        }
    }

    /**
     * ECU Stimulation Start/Stop:
     * - NEVER displays "via Bluetooth"
     * - If Bluetooth is NOT connected when Start or Stop is tapped, shows "Connect Bluetooth first" error.
     */
    fun toggleEcuStartStop() {
        if (bluetoothController.connectionState.value !is BtConnectionState.Connected) {
            _uiState.update {
                it.copy(
                    isSignalRunning = false,
                    showBtConnectFirstDialog = true,
                    statusBannerMessage = "Connect Bluetooth first"
                )
            }
            return
        }

        val nextRunning = !_uiState.value.isSignalRunning
        _uiState.update {
            it.copy(
                isSignalRunning = nextRunning,
                isSensorStimulationRunning = false,
                statusBannerMessage = if (nextRunning) "ECU Cranking Started" else "ECU Cranking Stopped"
            )
        }
        if (nextRunning) {
            transmitEcuFrame()
            recordEcuStimulationHistory()
        } else {
            bluetoothController.sendStopCommand()
        }
    }

    private fun stopEcuStimulationInternal() {
        _uiState.update { it.copy(isSignalRunning = false) }
        if (bluetoothController.connectionState.value is BtConnectionState.Connected) {
            bluetoothController.sendStopCommand()
        }
    }

    /**
     * Sensor Stimulation Start/Stop (NO Crank / NO Cam):
     * - Requires 1 or 2 ticked sensors
     * - If Bluetooth is NOT connected when Start or Stop is tapped, shows "Connect Bluetooth first" error.
     */
    fun toggleSensorStimulationStartStop() {
        if (bluetoothController.connectionState.value !is BtConnectionState.Connected) {
            _uiState.update {
                it.copy(
                    isSensorStimulationRunning = false,
                    showBtConnectFirstDialog = true,
                    statusBannerMessage = "Connect Bluetooth first"
                )
            }
            return
        }

        if (!_uiState.value.isSensorStimulationRunning && _uiState.value.sensorStimulationTicked.isEmpty()) {
            _uiState.update {
                it.copy(statusBannerMessage = "Tick 1 or 2 sensors first to start stimulation")
            }
            return
        }

        val nextRunning = !_uiState.value.isSensorStimulationRunning
        _uiState.update {
            it.copy(
                isSensorStimulationRunning = nextRunning,
                isSignalRunning = false,
                statusBannerMessage = if (nextRunning) "Sensor Stimulation Started" else "Sensor Stimulation Stopped"
            )
        }
        if (nextRunning) {
            transmitSensorOnlyFrame()
            recordSensorStimulationHistory()
        } else {
            bluetoothController.sendStopCommand()
        }
    }

    fun resetAllValuesToZero() {
        _uiState.update {
            it.copy(
                rpm = 0,
                railPressure = 0,
                ectTemp = 0,
                vehicleSpeed = 0,
                accelerator = 0,
                boostMapKpa = 0,
                mafGramsSec = 0,
                activeObdFaults = emptySet(),
                isSignalRunning = false,
                isSensorStimulationRunning = false,
                statusBannerMessage = "RESET COMPLETED: ALL VALUES SET TO 0"
            )
        }
        if (bluetoothController.connectionState.value is BtConnectionState.Connected) {
            bluetoothController.sendStopCommand()
            bluetoothController.sendObdFaultClearCommand()
            bluetoothController.sendRpmCommand(0)
            bluetoothController.sendRailCommand(0)
            bluetoothController.sendEctCommand(0)
            bluetoothController.sendSpeedCommand(0)
            bluetoothController.sendMapCommand(0)
            bluetoothController.sendMafCommand(0)
        }
    }

    fun dismissBtConnectFirstDialog() {
        _uiState.update { it.copy(showBtConnectFirstDialog = false) }
    }

    private fun transmitEcuFrame() {
        val s = _uiState.value
        val picked = s.ecuPickedSensors
        bluetoothController.sendEcuFrame(
            internalCrankPattern = s.selectedVehicle.crankPattern,
            rpm = s.rpm,
            railBar = if (StimulableSensor.RAIL_PRESSURE in picked) s.railPressure else s.railPressure,
            ectCelsius = if (StimulableSensor.COOLANT_TEMP in picked) s.ectTemp else 0,
            speedKmh = if (StimulableSensor.VEHICLE_SPEED in picked) s.vehicleSpeed else 0,
            accelPercent = s.accelerator,
            boostMapKpa = if (StimulableSensor.BOOST_PRESSURE in picked) s.boostMapKpa else 100,
            mafGramsSec = if (StimulableSensor.AIR_MASS in picked) s.mafGramsSec else 0,
            camSyncEnabled = s.camSyncEnabled,
            injectorPulseEnabled = s.injectorPulseEnabled,
            isRunning = s.isSignalRunning
        )
    }

    private fun transmitSensorOnlyFrame() {
        val s = _uiState.value
        bluetoothController.sendSensorOnlyFrame(
            selectedSensors = s.sensorStimulationTicked.map { it.id }.toSet(),
            railBar = s.railPressure,
            ectCelsius = s.ectTemp,
            speedKmh = s.vehicleSpeed,
            boostMapKpa = s.boostMapKpa,
            mafGramsSec = s.mafGramsSec,
            isRunning = s.isSensorStimulationRunning
        )
    }

    fun setShowBtQuickSheet(show: Boolean) {
        if (show) {
            bluetoothController.refreshPairedDevices()
        }
        _uiState.update { it.copy(showBtQuickSheet = show) }
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
                selectedLcvBrand = vehicle.brand,
                selectedCrankingMode = CrankingModePreset.CUSTOM,
                rpm = preset.rpm.coerceIn(RPM_MIN, RPM_MAX),
                railPressure = preset.railPressure.coerceIn(RAIL_MIN, RAIL_MAX),
                ectTemp = preset.ectTemp.coerceIn(ECT_MIN, ECT_MAX),
                vehicleSpeed = preset.vehicleSpeed.coerceIn(SPEED_MIN, SPEED_MAX),
                accelerator = preset.accelerator.coerceIn(ACCEL_MIN, ACCEL_MAX),
                boostMapKpa = preset.boostMapKpa.coerceIn(MAP_MIN, MAP_MAX),
                mafGramsSec = preset.mafGramsSec.coerceIn(MAF_MIN, MAF_MAX),
                camSyncEnabled = preset.camSyncEnabled,
                injectorPulseEnabled = preset.injectorPulseEnabled,
                currentRoute = AppRoute.EcuCrankingSetup,
                statusBannerMessage = "Loaded preset: ${preset.presetName}"
            )
        }
    }

    fun deletePreset(id: Int) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }

    private fun recordEcuStimulationHistory() {
        val s = _uiState.value
        val pickedSummary = if (s.ecuPickedSensors.isEmpty()) {
            "No Sensors"
        } else {
            s.ecuPickedSensors.joinToString(", ") { sensor ->
                "${sensor.shortLabel}: ${getSensorValue(sensor)} ${sensor.unit}"
            }
        }
        val dtcSuffix = if (s.activeObdFaults.isNotEmpty()) {
            " • DTC: ${s.activeObdFaults.joinToString(",") { it.code }}"
        } else {
            ""
        }
        viewModelScope.launch {
            historyRepository.addEntry(
                StimulationHistoryEntity(
                    modeType = "ECU STIMULATION",
                    vehicleLabel = "${s.selectedVehicle.brand} ${s.selectedVehicle.modelName}",
                    summary = "${s.selectedCrankingMode.title} • ${s.rpm} RPM • ${s.railPressure} bar • $pickedSummary$dtcSuffix",
                    rpm = s.rpm,
                    railPressure = s.railPressure,
                    sensorCount = s.ecuPickedSensors.size
                )
            )
        }
    }

    private fun recordSensorStimulationHistory() {
        val s = _uiState.value
        val sensorSummary = s.sensorStimulationTicked.joinToString(", ") { sensor ->
            "${sensor.shortLabel}: ${getSensorValue(sensor)} ${sensor.unit}"
        }
        viewModelScope.launch {
            historyRepository.addEntry(
                StimulationHistoryEntity(
                    modeType = "SENSOR STIMULATION",
                    vehicleLabel = "Sensor Only (No Crank / Cam)",
                    summary = sensorSummary.ifEmpty { "No Sensors Selected" },
                    rpm = 0,
                    railPressure = if (StimulableSensor.RAIL_PRESSURE in s.sensorStimulationTicked) s.railPressure else 0,
                    sensorCount = s.sensorStimulationTicked.size
                )
            )
        }
    }

    fun deleteHistoryEntry(id: Int) {
        viewModelScope.launch {
            historyRepository.deleteEntry(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
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
    }

    fun sendCustomSerialCommand(cmd: String) {
        bluetoothController.sendCustomRawCommand(cmd)
    }

    fun sendTestPing() {
        bluetoothController.sendTestPing()
    }

    fun clearBtLogs() {
        bluetoothController.clearLogs()
    }
}
