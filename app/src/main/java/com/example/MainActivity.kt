package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BootLoadingScreen
import com.example.ui.ComingSoonBrandsScreen
import com.example.ui.ContactScreen
import com.example.ui.DeviceScreen
import com.example.ui.EcuCategorySelectionScreen
import com.example.ui.EcuCrankingSetupScreen
import com.example.ui.EcuRunOnlyScreen
import com.example.ui.Hc05ConnectionBottomSheet
import com.example.ui.HomeScreen
import com.example.ui.LcvBrandsScreen
import com.example.ui.LcvVehiclesScreen
import com.example.ui.LogoSplashScreen
import com.example.ui.SavedPresetsScreen
import com.example.ui.SensorStimulationScreen
import com.example.ui.SettingsScreen
import com.example.ui.StimulationHistoryScreen
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.CardElevatedSurface
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MetalHighlightEdge
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.viewmodel.AppRoute
import com.example.viewmodel.BootStage
import com.example.viewmodel.EcuLabViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AkEcuStimulatorApp()
            }
        }
    }
}

@Composable
fun AkEcuStimulatorApp(
    viewModel: EcuLabViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val btState by viewModel.btConnectionState.collectAsStateWithLifecycle()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val isBtScanning by viewModel.isBtScanning.collectAsStateWithLifecycle()
    val autoReconnect by viewModel.autoReconnect.collectAsStateWithLifecycle()
    val packetMode by viewModel.packetMode.collectAsStateWithLifecycle()
    val txLog by viewModel.txLog.collectAsStateWithLifecycle()
    val packetsSent by viewModel.packetsSentCount.collectAsStateWithLifecycle()
    val packetsReceived by viewModel.packetsReceivedCount.collectAsStateWithLifecycle()
    val lastArduinoAck by viewModel.lastArduinoAck.collectAsStateWithLifecycle()
    val savedPresets by viewModel.savedPresets.collectAsStateWithLifecycle()
    val stimulationHistory by viewModel.stimulationHistory.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusBannerMessage) {
        val msg = uiState.statusBannerMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    if (uiState.bootStage == BootStage.Ready && uiState.currentRoute != AppRoute.Home) {
        BackHandler {
            viewModel.navigateBack()
        }
    }

    Crossfade(
        targetState = uiState.bootStage,
        animationSpec = tween(300),
        label = "boot_stage_transition"
    ) { stage ->
        when (stage) {
            BootStage.LogoSplash -> LogoSplashScreen()
            BootStage.BootLoading -> BootLoadingScreen()
            BootStage.Ready -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DeepObsidian,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        AkBottomNavBar(
                            currentRoute = uiState.currentRoute,
                            onSelectHome = { viewModel.navigateHome() },
                            onSelectEcu = { viewModel.openEcuStimulation() },
                            onSelectSensors = { viewModel.openSensorStimulation() },
                            onSelectSettings = { viewModel.navigateTo(AppRoute.Settings) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentRoute) {
                            AppRoute.Home -> {
                                HomeScreen(
                                    btState = btState,
                                    onTryAutoConnectBt = { viewModel.tryBluetoothAutoConnect() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onOpenSettings = { viewModel.navigateTo(AppRoute.Settings) },
                                    onSelectEcuStimulation = { viewModel.openEcuStimulation() },
                                    onSelectSensorStimulation = { viewModel.openSensorStimulation() }
                                )
                            }

                            AppRoute.EcuCategories -> {
                                EcuCategorySelectionScreen(
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onSelectCategory = { viewModel.selectVehicleCategory(it) }
                                )
                            }

                            AppRoute.ComingSoonBrands -> {
                                ComingSoonBrandsScreen(
                                    category = uiState.selectedCategory,
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) }
                                )
                            }

                            AppRoute.LcvBrands -> {
                                LcvBrandsScreen(
                                    selectedLcvBrand = uiState.selectedLcvBrand,
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onSelectBrand = { viewModel.selectLcvBrand(it) }
                                )
                            }

                            AppRoute.LcvVehicles -> {
                                LcvVehiclesScreen(
                                    brand = uiState.selectedLcvBrand,
                                    selectedVehicle = uiState.selectedVehicle,
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onSelectVehicle = { viewModel.selectLcvVehicle(it) }
                                )
                            }

                            AppRoute.EcuCrankingSetup -> {
                                EcuCrankingSetupScreen(
                                    uiState = uiState,
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onSelectVehicleSwitch = { viewModel.navigateTo(AppRoute.LcvBrands) },
                                    onSelectCrankingMode = { viewModel.selectCrankingMode(it) },
                                    onRpmChange = { viewModel.updateRpm(it) },
                                    onRailChange = { viewModel.updateRailPressure(it) },
                                    onCamSyncToggle = { viewModel.toggleCamSync(it) },
                                    onToggleSensorPick = { viewModel.toggleEcuPickedSensor(it) },
                                    onSensorValueChange = { sensor, v -> viewModel.updateSensorValue(sensor, v) },
                                    onProceedToStartPage = { viewModel.proceedToEcuStartPage() },
                                    onResetAllToZero = { viewModel.resetAllValuesToZero() },
                                    onTapSavePreset = { viewModel.openSavePresetDialog() },
                                    onSavePresetConfirm = { viewModel.saveCurrentPreset(it) },
                                    onDismissSaveDialog = { viewModel.dismissSavePresetDialog() }
                                )
                            }

                            AppRoute.EcuRunPage -> {
                                EcuRunOnlyScreen(
                                    uiState = uiState,
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onToggleStartStop = { viewModel.toggleEcuStartStop() },
                                    onResetAllToZero = { viewModel.resetAllValuesToZero() }
                                )
                            }

                            AppRoute.SensorStimulation -> {
                                SensorStimulationScreen(
                                    uiState = uiState,
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onToggleTickSensor = { viewModel.toggleSensorStimulationTick(it) },
                                    onSensorValueChange = { sensor, v -> viewModel.updateSensorValue(sensor, v) },
                                    onToggleStartStop = { viewModel.toggleSensorStimulationStartStop() },
                                    onResetAllToZero = { viewModel.resetAllValuesToZero() }
                                )
                            }

                            AppRoute.BluetoothDevices -> {
                                DeviceScreen(
                                    btState = btState,
                                    pairedDevices = pairedDevices,
                                    discoveredDevices = discoveredDevices,
                                    isScanning = isBtScanning,
                                    autoReconnect = autoReconnect,
                                    packetMode = packetMode,
                                    txLog = txLog,
                                    packetsSent = packetsSent,
                                    packetsReceived = packetsReceived,
                                    lastDeviceAck = lastArduinoAck,
                                    onRefreshPairedDevices = { viewModel.refreshBluetoothDevices() },
                                    onStartDiscoveryScan = { viewModel.startBtDiscoveryScan() },
                                    onStopDiscoveryScan = { viewModel.stopBtDiscoveryScan() },
                                    onConnectDevice = { viewModel.connectHc05(it) },
                                    onPairAndConnectDevice = { viewModel.pairAndConnectHc05(it) },
                                    onDisconnect = { viewModel.disconnectHc05() },
                                    onToggleAutoReconnect = { viewModel.setAutoReconnect(it) },
                                    onSelectPacketMode = { viewModel.setSerialPacketMode(it) },
                                    onSendCustomCommand = { viewModel.sendCustomSerialCommand(it) },
                                    onSendTestPing = { viewModel.sendTestPing() },
                                    onClearLogs = { viewModel.clearBtLogs() },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            AppRoute.SavedPresets -> {
                                SavedPresetsScreen(
                                    presets = savedPresets,
                                    onLoadPreset = { viewModel.loadPreset(it) },
                                    onDeletePreset = { viewModel.deletePreset(it) },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            AppRoute.StimulationHistory -> {
                                StimulationHistoryScreen(
                                    historyEntries = stimulationHistory,
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) },
                                    onDeleteEntry = { viewModel.deleteHistoryEntry(it) },
                                    onClearAll = { viewModel.clearAllHistory() }
                                )
                            }

                            AppRoute.ContactInfo -> {
                                ContactScreen(
                                    btState = btState,
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSheet = { viewModel.setShowBtQuickSheet(true) }
                                )
                            }

                            AppRoute.Settings -> {
                                SettingsScreen(
                                    btState = btState,
                                    autoReconnect = autoReconnect,
                                    onToggleAutoReconnect = { viewModel.setAutoReconnect(it) },
                                    onBack = { viewModel.navigateBack() },
                                    onOpenBluetoothSettings = { viewModel.navigateTo(AppRoute.BluetoothDevices) },
                                    onOpenSavedPresets = { viewModel.navigateTo(AppRoute.SavedPresets) },
                                    onOpenHistory = { viewModel.navigateTo(AppRoute.StimulationHistory) },
                                    onOpenContact = { viewModel.navigateTo(AppRoute.ContactInfo) }
                                )
                            }
                        }
                    }

                    if (uiState.showBtQuickSheet) {
                        Hc05ConnectionBottomSheet(
                            btState = btState,
                            pairedDevices = pairedDevices,
                            discoveredDevices = discoveredDevices,
                            isScanning = isBtScanning,
                            packetsSent = packetsSent,
                            packetsReceived = packetsReceived,
                            onDismiss = { viewModel.setShowBtQuickSheet(false) },
                            onStartScan = { viewModel.startBtDiscoveryScan() },
                            onStopScan = { viewModel.stopBtDiscoveryScan() },
                            onConnectPaired = {
                                viewModel.connectHc05(it)
                                viewModel.setShowBtQuickSheet(false)
                            },
                            onPairAndConnect = {
                                viewModel.pairAndConnectHc05(it)
                            },
                            onDisconnect = { viewModel.disconnectHc05() },
                            onSendTestPing = { viewModel.sendTestPing() },
                            onGoToDeviceTab = {
                                viewModel.setShowBtQuickSheet(false)
                                viewModel.navigateTo(AppRoute.BluetoothDevices)
                            }
                        )
                    }

                    if (uiState.showBtConnectFirstDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.dismissBtConnectFirstDialog() },
                            containerColor = CardElevatedSurface,
                            titleContentColor = TextWhite,
                            textContentColor = TextSecondary,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.BluetoothDisabled,
                                    contentDescription = "Connect Bluetooth First",
                                    tint = StopRed
                                )
                            },
                            title = {
                                Text(
                                    text = "Connect Bluetooth first",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    text = "Bluetooth hardware is not connected. Please connect your AK ECU Stimulator Bluetooth unit before starting or stopping stimulation.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.dismissBtConnectFirstDialog()
                                        viewModel.setShowBtQuickSheet(true)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    modifier = Modifier.testTag("bt_connect_first_open_bt")
                                ) {
                                    Text("CONNECT BLUETOOTH", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.dismissBtConnectFirstDialog() }) {
                                    Text("CLOSE", color = TextSecondary)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AkBottomNavBar(
    currentRoute: AppRoute,
    onSelectHome: () -> Unit,
    onSelectEcu: () -> Unit,
    onSelectSensors: () -> Unit,
    onSelectSettings: () -> Unit
) {
    val isEcuRoute = currentRoute in listOf(
        AppRoute.EcuCategories,
        AppRoute.ComingSoonBrands,
        AppRoute.LcvBrands,
        AppRoute.LcvVehicles,
        AppRoute.EcuCrankingSetup,
        AppRoute.EcuRunPage
    )
    val isSettingsRoute = currentRoute in listOf(
        AppRoute.Settings,
        AppRoute.BluetoothDevices,
        AppRoute.SavedPresets,
        AppRoute.StimulationHistory,
        AppRoute.ContactInfo
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MetalHighlightEdge)
        )
        NavigationBar(
            containerColor = CardDarkSurface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars
        ) {
            val navItems = listOf(
                Triple("Home", currentRoute == AppRoute.Home, Pair(Icons.Filled.Home, Icons.Outlined.Home)) to onSelectHome,
                Triple("ECU", isEcuRoute, Pair(Icons.Filled.Memory, Icons.Outlined.Memory)) to onSelectEcu,
                Triple("Sensors", currentRoute == AppRoute.SensorStimulation, Pair(Icons.Filled.Sensors, Icons.Outlined.Sensors)) to onSelectSensors,
                Triple("Settings", isSettingsRoute, Pair(Icons.Filled.Settings, Icons.Outlined.Settings)) to onSelectSettings
            )

            navItems.forEach { (meta, onClick) ->
                val (label, selected, icons) = meta
                NavigationBarItem(
                    selected = selected,
                    onClick = onClick,
                    icon = {
                        Icon(
                            imageVector = if (selected) icons.first else icons.second,
                            contentDescription = label
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanGlow,
                        selectedTextColor = CyanGlow,
                        indicatorColor = RecessedWell,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                )
            }
        }
    }
}
