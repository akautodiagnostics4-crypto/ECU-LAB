package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BootLoadingScreen
import com.example.ui.ControlScreen
import com.example.ui.DeviceScreen
import com.example.ui.Hc05ConnectionBottomSheet
import com.example.ui.Hc05StatusIndicatorBar
import com.example.ui.LoginScreen
import com.example.ui.LogoSplashScreen
import com.example.ui.SavedPresetsScreen
import com.example.ui.VehiclesScreen
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AccentColorTheme
import com.example.viewmodel.BootStage
import com.example.viewmodel.EcuLabTab
import com.example.viewmodel.EcuLabViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EcuLabApp()
            }
        }
    }
}

@Composable
fun EcuLabApp(
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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusBannerMessage) {
        val msg = uiState.statusBannerMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    // Handle Android system back button on sub-tabs when Ready
    if (uiState.bootStage == BootStage.Ready && uiState.activeTab != EcuLabTab.Vehicles) {
        BackHandler {
            viewModel.selectTab(EcuLabTab.Vehicles)
        }
    }

    Crossfade(
        targetState = uiState.bootStage,
        animationSpec = tween(300),
        label = "boot_stage_transition"
    ) { stage ->
        when (stage) {
            BootStage.LogoSplash -> {
                LogoSplashScreen()
            }

            BootStage.BootLoading -> {
                BootLoadingScreen()
            }

            BootStage.Ready -> {
                if (!uiState.isLoggedIn) {
                    LoginScreen(
                        loginError = uiState.loginError,
                        onLoginSubmit = { user, pass, rememberMe ->
                            viewModel.login(user, pass, rememberMe)
                        },
                        onClearError = { viewModel.clearLoginError() }
                    )
                } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DeepObsidian,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        EcuLabBottomBar(
                            activeTab = uiState.activeTab,
                            onSelectTab = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Global HC-05 Connection Status Indicator Bar across all tabs
                        Hc05StatusIndicatorBar(
                            btState = btState,
                            packetsSent = packetsSent,
                            packetsReceived = packetsReceived,
                            onOpenQuickManager = { viewModel.setShowBtQuickSheet(true) },
                            onQuickAction = { viewModel.quickConnectHc05() }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            when (uiState.activeTab) {
                                EcuLabTab.Control -> {
                                    ControlScreen(
                                        uiState = uiState,
                                        btState = btState,
                                        onTapSelectVehicle = { viewModel.selectTab(EcuLabTab.Vehicles) },
                                        onTapSavePreset = { viewModel.openSavePresetDialog() },
                                        onTapBtStatus = { viewModel.setShowBtQuickSheet(true) },
                                        onRpmChange = { viewModel.updateRpm(it) },
                                        onRailChange = { viewModel.updateRailPressure(it) },
                                        onEctChange = { viewModel.updateEctTemp(it) },
                                        onSpeedChange = { viewModel.updateVehicleSpeed(it) },
                                        onAccelChange = { viewModel.updateAccelerator(it) },
                                        onBoostMapChange = { viewModel.updateBoostMap(it) },
                                        onMafChange = { viewModel.updateMaf(it) },
                                        onCamSyncToggle = { viewModel.toggleCamSync(it) },
                                        onInjectorPulseToggle = { viewModel.toggleInjectorPulse(it) },
                                        onApplyQuickMode = { viewModel.applyQuickBenchMode(it) },
                                        onCycleAccentTheme = {
                                            val all = AccentColorTheme.entries
                                            val next = all[(all.indexOf(uiState.accentTheme) + 1) % all.size]
                                            viewModel.setAccentTheme(next)
                                        },
                                        onToggleStartStop = { viewModel.toggleSignalGeneration() },
                                        onSavePresetConfirm = { viewModel.saveCurrentPreset(it) },
                                        onDismissSaveDialog = { viewModel.dismissSavePresetDialog() }
                                    )
                                }

                                EcuLabTab.Vehicles -> {
                                    VehiclesScreen(
                                        selectedVehicle = uiState.selectedVehicle,
                                        selectedBrandFilter = uiState.selectedBrandFilter,
                                        searchQuery = uiState.searchQuery,
                                        onBrandFilterChange = { viewModel.setBrandFilter(it) },
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        onSelectVehicle = { viewModel.selectVehicle(it) }
                                    )
                                }

                                EcuLabTab.Saved -> {
                                    SavedPresetsScreen(
                                        presets = savedPresets,
                                        onLoadPreset = { viewModel.loadPreset(it) },
                                        onDeletePreset = { viewModel.deletePreset(it) }
                                    )
                                }

                                EcuLabTab.Device -> {
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
                                        loggedInUsername = uiState.loggedInUsername,
                                        showChangeCredentialsDialog = uiState.showChangeCredentialsDialog,
                                        onOpenChangeCredentials = { viewModel.setShowChangeCredentialsDialog(true) },
                                        onDismissChangeCredentials = { viewModel.setShowChangeCredentialsDialog(false) },
                                        onSaveNewCredentials = { newUser, newPass ->
                                            viewModel.updateLoginCredentials(newUser, newPass)
                                        },
                                        onLogout = { viewModel.logout() }
                                    )
                                }
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
                            onGoToDeviceTab = { viewModel.selectTab(EcuLabTab.Device) }
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun EcuLabBottomBar(
    activeTab: EcuLabTab,
    onSelectTab: (EcuLabTab) -> Unit
) {
    NavigationBar(
        containerColor = CardDarkSurface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val items = listOf(
            Triple(EcuLabTab.Control, "Control", Pair(Icons.Filled.Tune, Icons.Outlined.Tune)),
            Triple(EcuLabTab.Vehicles, "Vehicles", Pair(Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar)),
            Triple(EcuLabTab.Saved, "Saved", Pair(Icons.Filled.Folder, Icons.Outlined.Folder)),
            Triple(EcuLabTab.Device, "Device", Pair(Icons.Filled.Bluetooth, Icons.Outlined.Bluetooth))
        )

        items.forEach { (tab, label, icons) ->
            val selected = activeTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
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
                    selectedIconColor = ElectricBlue,
                    selectedTextColor = ElectricBlue,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
            )
        }
    }
}
