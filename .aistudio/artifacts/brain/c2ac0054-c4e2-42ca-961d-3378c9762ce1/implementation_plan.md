# ECT Range 0-125°C, Master Zero Reset Button & Enhanced Cockpit Features

This plan updates the Engine Coolant Temperature (ECT) range from 0-200°C to **0-125°C**, adds a prominent **RESET TO ZERO** button that zeroes all bench parameters in one tap, and introduces smart automotive safety telemetry warnings for engine over-temperature and cold/operating status.

### User Review & Critical Decisions

> [!IMPORTANT]
> - **ECT Range**: Changed to **0 to 125 °C** across the slider, stepper buttons, and live cluster gauge.
> - **Master Reset Button ("RESET ALL = 0")**: Placed directly in the Control header and next to the Start/Stop footer. When tapped, it immediately sets RPM = 0, Rail = 0, ECT = 0, Speed = 0, Accelerator = 0, Boost MAP = 0 (or baseline), MAF = 0, stops signal transmission, and sends matching zero commands via Bluetooth serial.
> - **Extra Features Included**:
>   1. **Overheat Warning Visual Alert**: When ECT exceeds 105°C, gauge and pill show a flashing amber/red caution state (`ENGINE OVERHEAT WARNING`).
>   2. **ECT Quick Presets**: Cold Start (20°C), Warm Operating (85°C), Thermostat Open (95°C), and Fan Trigger (105°C) in the quick bench row.
>   3. **Live Telemetry Ramp & Reset Confirmation**: Clear visual feedback when resetting all values to 0.

---

### 1. Overview & Core Concept

- **What It Does**:
  - Restricts the ECT (Coolant Temperature) range strictly to **0 to 125 °C**, matching common NTC thermistor sensor ranges used in automotive diagnostic benches.
  - Adds a dedicated, high-contrast **RESET ALL (0)** button that zeroes out all sensor values and stops signal transmission.
  - Enhances live bench telemetry with temperature zone status indicators (Cold / Optimal / Overheat).
- **Target Audience**: Automotive ECU technicians, lab testers, and calibration engineers using Arduino Uno + HC-05 test benches.
- **Key Value**: Prevents out-of-spec temperature simulation while providing an instant safety emergency zero-reset during high-power signal tests.

---

### 2. User Experience & Visual Design

```
┌─────────────────────────────────────────────────────────────┐
│ ECU SIGNAL LAB PRO       [RESET = 0]  [THEME]  [SAVE]       │
│ Mahindra • Bolero 60-2                                      │
├─────────────────────────────────────────────────────────────┤
│ [STOPPED / RUNNING]    [BLUETOOTH CONNECTED]                │
├─────────────────────────────────────────────────────────────┤
│ ┌─ LIVE BENCH TELEMETRY CLUSTER ──────────────────────────┐ │
│ │  (RPM 0-2500)      (RAIL 0-1200 bar)   (ECT 0-125°C)   │ │
│ │      [800]              [300]              [85]         │ │
│ │  ECT STATUS: OPTIMAL (85°C) • FAN OFF                   │ │
│ └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│ QUICK MODES: [RESET 0] [CRANK 250] [IDLE 85°C] [FULL LOAD] │
├─────────────────────────────────────────────────────────────┤
│ [PARAM CARDS: RPM, RAIL 0-1200b, ECT 0-125°C, SPEED, ...]   │
├─────────────────────────────────────────────────────────────┤
│ [ RESET ALL VALUES = 0 ]          [ START / STOP SIGNAL ]   │
└─────────────────────────────────────────────────────────────┘
```

- **Top Action Bar**: A dedicated red/amber-bordered **RESET (0)** button with refresh icon.
- **ECT Card**: Shows `0-125 °C` range with dynamic gauge color:
  - Blue (`< 50°C`: Cold Engine)
  - Diagnostic Green (`50°C - 102°C`: Normal Operating Temp)
  - Red / Amber (`103°C - 125°C`: High Temp / Overheat Alert)
- **Footer Controls**: Split action bar with **RESET ALL (0)** on the left and **START / STOP** on the right.

---

### 3. Key Product Decisions & Trade-Offs

- **Reset Scope**:
  - Sets `rpm = 0`, `railPressure = 0`, `ectTemp = 0`, `vehicleSpeed = 0`, `accelerator = 0`, `boostMapKpa = 0` (or min 0 for full reset), `mafGramsSec = 0`, and `isSignalRunning = false`.
  - Sends immediate serial frames via Bluetooth (`STOP\n`, `RPM=0\n`, `RAIL=0\n`, `ECT=0\n`, etc.) to guarantee the bench hardware immediately silences all pulse generation.
- **ECT Maximum Bound (125°C)**:
  - Constrained in `EcuLabViewModel.kt` (`ECT_MIN = 0`, `ECT_MAX = 125`).
  - Sliders, steppers, and gauge progress calculations dynamically bind to `(ect - 0f) / (125f - 0f)`.

---

### 4. Technical Architecture & Component Mapping

```
┌──────────────────────────────────────────────────────────┐
│                      EcuLabViewModel                     │
│  - ECT_MIN = 0, ECT_MAX = 125                            │
│  - resetAllValuesToZero(): zeroes all parameters + STOP  │
│  - transmitCurrentFrame() & sendKeyValueCommands()       │
└────────────────────────────┬─────────────────────────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
┌───────────────────────────────┐ ┌───────────────────────────┐
│         ControlScreen         │ │      VehicleCatalog       │
│ - Reset button in header      │ │ - Updated default ECTs    │
│ - Split Reset/StartStop footer│ │   clamped to max 125°C    │
│ - ECT 0-125°C gauge & card    │ └───────────────────────────┘
│ - Overheat warning pill       │
└───────────────────────────────┘
```

- **Files to Modify**:
  1. `com/example/viewmodel/EcuLabViewModel.kt`:
     - Update `ECT_MAX = 125`.
     - Implement `resetAllValuesToZero()` method.
     - Add `reset` serial broadcast to Bluetooth.
  2. `com/example/ui/ControlScreen.kt`:
     - Add Reset button in `ControlTopHeader`.
     - Update `ParameterAdjustmentCard` and `CircularMiniGauge` for ECT (0 to 125 °C).
     - Add ECT over-temp alert badge in gauge cluster.
     - Add secondary Reset button in bottom action bar.
  3. `com/example/data/EcuLabData.kt`:
     - Ensure all preset vehicle defaults clamp ECT within 0-125°C.
