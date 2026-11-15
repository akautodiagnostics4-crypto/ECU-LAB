package com.example.data

enum class ObdFaultSeverity(val label: String) {
    WARNING("WARNING"),
    CRITICAL("CRITICAL")
}

enum class ObdFaultCategory(val label: String) {
    ALL("ALL"),
    RAIL("RAIL"),
    ECT("ECT"),
    MAP("MAP"),
    MAF("MAF"),
    VSS("VSS"),
    CRANK_CAM("CRANK/CAM")
}

/**
 * Simulated OBD-II Diagnostic Trouble Codes (DTCs) for testing ECU responses
 * to common diesel/CRDI sensor and synchronization faults in ECU Stimulation mode.
 */
enum class ObdFaultCode(
    val code: String,
    val title: String,
    val category: ObdFaultCategory,
    val targetSensor: StimulableSensor?,
    val severity: ObdFaultSeverity,
    val triggersLimpMode: Boolean,
    val simulatedSensorValue: Int?,
    val disablesCamSync: Boolean = false,
    val simulatedRpmOverride: Int? = null,
    val faultConditionSummary: String,
    val expectedEcuResponse: String
) {
    P0087_RAIL_TOO_LOW(
        code = "P0087",
        title = "Fuel Rail / System Pressure - Too Low",
        category = ObdFaultCategory.RAIL,
        targetSensor = StimulableSensor.RAIL_PRESSURE,
        severity = ObdFaultSeverity.CRITICAL,
        triggersLimpMode = true,
        simulatedSensorValue = 80,
        faultConditionSummary = "Drops simulated Rail Pressure to 80 bar",
        expectedEcuResponse = "MIL ON • Derates torque, caps RPM ~1500, increases SCV duty cycle"
    ),
    P0088_RAIL_TOO_HIGH(
        code = "P0088",
        title = "Fuel Rail / System Pressure - Too High",
        category = ObdFaultCategory.RAIL,
        targetSensor = StimulableSensor.RAIL_PRESSURE,
        severity = ObdFaultSeverity.CRITICAL,
        triggersLimpMode = true,
        simulatedSensorValue = 1150,
        faultConditionSummary = "Spikes Rail Pressure to 1150 bar",
        expectedEcuResponse = "MIL ON • Opens rail pressure relief, cuts injector duration / engine shutdown risk"
    ),
    P0193_RAIL_CIRCUIT_HIGH(
        code = "P0193",
        title = "Fuel Rail Pressure Sensor A Circuit High",
        category = ObdFaultCategory.RAIL,
        targetSensor = StimulableSensor.RAIL_PRESSURE,
        severity = ObdFaultSeverity.CRITICAL,
        triggersLimpMode = true,
        simulatedSensorValue = 1200,
        faultConditionSummary = "Forces Rail Sensor to 1200 bar (5V open/short)",
        expectedEcuResponse = "MIL ON • Switches to open-loop rail fallback pressure & limp mode"
    ),
    P0118_ECT_CIRCUIT_HIGH(
        code = "P0118",
        title = "Engine Coolant Temp Sensor Circuit High",
        category = ObdFaultCategory.ECT,
        targetSensor = StimulableSensor.COOLANT_TEMP,
        severity = ObdFaultSeverity.WARNING,
        triggersLimpMode = false,
        simulatedSensorValue = 0,
        faultConditionSummary = "Forces ECT reading to 0 °C (open circuit)",
        expectedEcuResponse = "MIL ON • Activates cold-start glow strategy & forces cooling fan ON"
    ),
    P0117_ECT_CIRCUIT_LOW(
        code = "P0117",
        title = "Engine Coolant Temp Sensor Circuit Low",
        category = ObdFaultCategory.ECT,
        targetSensor = StimulableSensor.COOLANT_TEMP,
        severity = ObdFaultSeverity.CRITICAL,
        triggersLimpMode = true,
        simulatedSensorValue = 125,
        faultConditionSummary = "Drives ECT to 125 °C (overheat / short to ground)",
        expectedEcuResponse = "MIL ON • Overheat protection derate, cooling fan 100%, A/C compressor cut"
    ),
    P0237_BOOST_LOW(
        code = "P0237",
        title = "Turbocharger Boost Sensor A Circuit Low",
        category = ObdFaultCategory.MAP,
        targetSensor = StimulableSensor.BOOST_PRESSURE,
        severity = ObdFaultSeverity.WARNING,
        triggersLimpMode = true,
        simulatedSensorValue = 100,
        faultConditionSummary = "Clamps Boost MAP to 100 kPa (atmospheric/no boost)",
        expectedEcuResponse = "MIL ON • Disables VGT boost control, limits fuel quantity (smoke limiter)"
    ),
    P0238_BOOST_HIGH(
        code = "P0238",
        title = "Turbocharger Boost Sensor A Circuit High",
        category = ObdFaultCategory.MAP,
        targetSensor = StimulableSensor.BOOST_PRESSURE,
        severity = ObdFaultSeverity.CRITICAL,
        triggersLimpMode = true,
        simulatedSensorValue = 295,
        faultConditionSummary = "Spikes Boost MAP to 295 kPa (overboost fault)",
        expectedEcuResponse = "MIL ON • Immediate overboost fuel cut & limp-home boost reduction"
    ),
    P0102_MAF_LOW(
        code = "P0102",
        title = "Mass Air Flow (MAF) Circuit Low Input",
        category = ObdFaultCategory.MAF,
        targetSensor = StimulableSensor.AIR_MASS,
        severity = ObdFaultSeverity.WARNING,
        triggersLimpMode = false,
        simulatedSensorValue = 0,
        faultConditionSummary = "Drops Air Mass Flow to 0 g/s",
        expectedEcuResponse = "MIL ON • Falls back to speed-density MAP model & disables EGR loop"
    ),
    P0103_MAF_HIGH(
        code = "P0103",
        title = "Mass Air Flow (MAF) Circuit High Input",
        category = ObdFaultCategory.MAF,
        targetSensor = StimulableSensor.AIR_MASS,
        severity = ObdFaultSeverity.WARNING,
        triggersLimpMode = true,
        simulatedSensorValue = 480,
        faultConditionSummary = "Drives Air Mass Flow to 480 g/s",
        expectedEcuResponse = "MIL ON • EGR disabled, smoke-map fuel limiting active"
    ),
    P0500_VSS_MALFUNCTION(
        code = "P0500",
        title = "Vehicle Speed Sensor A Malfunction",
        category = ObdFaultCategory.VSS,
        targetSensor = StimulableSensor.VEHICLE_SPEED,
        severity = ObdFaultSeverity.WARNING,
        triggersLimpMode = false,
        simulatedSensorValue = 0,
        faultConditionSummary = "Forces Vehicle Speed signal to 0 km/h",
        expectedEcuResponse = "MIL ON • Cruise/PTO inhibited, idle-up speed compensation disabled"
    ),
    P0340_CAM_SYNC_FAULT(
        code = "P0340",
        title = "Camshaft Position Sensor A Circuit",
        category = ObdFaultCategory.CRANK_CAM,
        targetSensor = null,
        severity = ObdFaultSeverity.CRITICAL,
        triggersLimpMode = true,
        simulatedSensorValue = null,
        disablesCamSync = true,
        faultConditionSummary = "Drops Camshaft Sync signal (Crank Only)",
        expectedEcuResponse = "MIL ON • Extended cranking or no-start / single-bank sequential fallback"
    ),
    P0335_CRANK_SIGNAL_LOSS(
        code = "P0335",
        title = "Crankshaft Position Sensor A Circuit",
        category = ObdFaultCategory.CRANK_CAM,
        targetSensor = null,
        severity = ObdFaultSeverity.CRITICAL,
        triggersLimpMode = true,
        simulatedSensorValue = null,
        simulatedRpmOverride = 200,
        faultConditionSummary = "Drops Engine RPM to 200 RPM crank stall threshold",
        expectedEcuResponse = "MIL ON • Loss of crank sync, ECU shuts off high-pressure injector firing"
    )
}

data class ObdSimulatedResponseSummary(
    val milLampOn: Boolean,
    val limpModeActive: Boolean,
    val coolingFanForced: Boolean,
    val fuelCutRisk: Boolean,
    val egrInhibited: Boolean,
    val activeCodesLabel: String,
    val diagnosticAdvisory: String
)

object ObdFaultCatalog {

    fun byCategory(category: ObdFaultCategory): List<ObdFaultCode> {
        return if (category == ObdFaultCategory.ALL) {
            ObdFaultCode.entries
        } else {
            ObdFaultCode.entries.filter { it.category == category }
        }
    }

    fun pickRandomCandidate(currentlyActive: Set<ObdFaultCode>): ObdFaultCode {
        val remaining = ObdFaultCode.entries.filterNot { it in currentlyActive }
        val pool = if (remaining.isNotEmpty()) remaining else ObdFaultCode.entries
        return pool.random()
    }

    fun evaluateEcuResponse(activeFaults: Set<ObdFaultCode>): ObdSimulatedResponseSummary {
        if (activeFaults.isEmpty()) {
            return ObdSimulatedResponseSummary(
                milLampOn = false,
                limpModeActive = false,
                coolingFanForced = false,
                fuelCutRisk = false,
                egrInhibited = false,
                activeCodesLabel = "NO ACTIVE DTC",
                diagnosticAdvisory = "Normal closed-loop ECU operation. Inject a DTC below to test sensor fault reaction."
            )
        }

        val limpMode = activeFaults.any { it.triggersLimpMode }
        val coolingFan = activeFaults.any {
            it == ObdFaultCode.P0117_ECT_CIRCUIT_LOW || it == ObdFaultCode.P0118_ECT_CIRCUIT_HIGH
        }
        val fuelCut = activeFaults.any {
            it == ObdFaultCode.P0088_RAIL_TOO_HIGH ||
                it == ObdFaultCode.P0238_BOOST_HIGH ||
                it == ObdFaultCode.P0335_CRANK_SIGNAL_LOSS
        }
        val egrOff = activeFaults.any {
            it == ObdFaultCode.P0102_MAF_LOW ||
                it == ObdFaultCode.P0103_MAF_HIGH ||
                it == ObdFaultCode.P0237_BOOST_LOW
        }

        val codes = activeFaults.joinToString(", ") { it.code }
        val advisory = activeFaults.last().expectedEcuResponse

        return ObdSimulatedResponseSummary(
            milLampOn = true,
            limpModeActive = limpMode,
            coolingFanForced = coolingFan,
            fuelCutRisk = fuelCut,
            egrInhibited = egrOff,
            activeCodesLabel = codes,
            diagnosticAdvisory = advisory
        )
    }
}
