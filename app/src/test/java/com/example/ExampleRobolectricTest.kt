package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.StimulableSensor
import com.example.data.VehicleCatalog
import com.example.data.VehicleCategoryType
import com.example.viewmodel.AppRoute
import com.example.viewmodel.CrankingModePreset
import com.example.viewmodel.EcuLabViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AK ECU STIMULATOR", appName)
  }

  @Test
  fun `vehicle categories and lcv brands match specification`() {
    assertTrue(VehicleCategoryType.CAR.isComingSoon)
    assertFalse(VehicleCategoryType.LCV.isComingSoon)
    assertTrue(VehicleCategoryType.HCV.isComingSoon)
    assertTrue(VehicleCategoryType.BUS.isComingSoon)

    assertEquals(
      listOf("TATA", "MAHINDRA", "ASHOK LEYLAND", "FORCE", "BHARATBENZ"),
      VehicleCatalog.lcvBrands
    )
    VehicleCatalog.lcvBrands.forEach { brand ->
      assertTrue(
        "Expected vehicles for LCV brand $brand",
        VehicleCatalog.vehiclesForLcvBrand(brand).isNotEmpty()
      )
    }
  }

  @Test
  fun `cranking modes and bluetooth connect first gate work as expected`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = EcuLabViewModel(app)

    vm.selectCrankingMode(CrankingModePreset.MODE_1)
    assertEquals(300, vm.uiState.value.rpm)
    assertEquals(250, vm.uiState.value.railPressure)

    vm.selectCrankingMode(CrankingModePreset.MODE_2)
    assertEquals(700, vm.uiState.value.rpm)
    assertEquals(350, vm.uiState.value.railPressure)

    vm.selectCrankingMode(CrankingModePreset.MODE_3)
    assertEquals(1200, vm.uiState.value.rpm)
    assertEquals(600, vm.uiState.value.railPressure)

    // Without Bluetooth connected, Start triggers "Connect Bluetooth first" and does not start
    vm.toggleEcuStartStop()
    assertFalse(vm.uiState.value.isSignalRunning)
    assertTrue(vm.uiState.value.showBtConnectFirstDialog)
    assertEquals("Connect Bluetooth first", vm.uiState.value.statusBannerMessage)

    // Reset sets all telemetry values to 0
    vm.resetAllValuesToZero()
    assertEquals(0, vm.uiState.value.rpm)
    assertEquals(0, vm.uiState.value.railPressure)
    assertEquals(0, vm.uiState.value.ectTemp)
    assertEquals(0, vm.uiState.value.vehicleSpeed)
    assertEquals(0, vm.uiState.value.boostMapKpa)
    assertEquals(0, vm.uiState.value.mafGramsSec)
  }

  @Test
  fun `sensor stimulation mode allows selecting multiple sensors freely`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = EcuLabViewModel(app)

    vm.openSensorStimulation()
    assertEquals(AppRoute.SensorStimulation, vm.uiState.value.currentRoute)
    assertEquals(2, vm.uiState.value.sensorStimulationTicked.size)

    // Selecting additional sensors succeeds without restriction or warning
    vm.toggleSensorStimulationTick(StimulableSensor.VEHICLE_SPEED)
    vm.toggleSensorStimulationTick(StimulableSensor.BOOST_PRESSURE)
    assertEquals(4, vm.uiState.value.sensorStimulationTicked.size)
    assertTrue(StimulableSensor.VEHICLE_SPEED in vm.uiState.value.sensorStimulationTicked)
    assertTrue(StimulableSensor.BOOST_PRESSURE in vm.uiState.value.sensorStimulationTicked)
    assertEquals(null, vm.uiState.value.statusBannerMessage)
  }

  @Test
  fun `ecu cranking mode allows selecting multiple picked sensors freely`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = EcuLabViewModel(app)

    // Initially 1 picked sensor (RAIL_PRESSURE)
    assertEquals(1, vm.uiState.value.ecuPickedSensors.size)

    vm.toggleEcuPickedSensor(StimulableSensor.COOLANT_TEMP)
    vm.toggleEcuPickedSensor(StimulableSensor.AIR_MASS)
    assertEquals(3, vm.uiState.value.ecuPickedSensors.size)
    assertTrue(StimulableSensor.AIR_MASS in vm.uiState.value.ecuPickedSensors)
    assertEquals(null, vm.uiState.value.statusBannerMessage)
  }
}
