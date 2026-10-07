package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Represents a Vehicle Profile in ECU LAB.
 * NOTE: Per user requirement ("DONT SHOW THE CRANK TOOTH PATTERN ON THE DISPLAY"),
 * [crankPattern] is kept strictly internal for HC-05 serial protocol transmission
 * to the Arduino Uno and is NEVER rendered on the UI display.
 */
data class VehicleModel(
    val id: String,
    val brand: String,
    val modelName: String,
    val category: String, // CAR, LCV, HCV, BUS, SUV, VAN
    val ecuSystem: String,
    internal val crankPattern: String, // Hidden from display! Used only for Arduino HC-05 packet
    val defaultRpm: Int = 800,
    val defaultRail: Int = 300,
    val defaultEct: Int = 90,
    val defaultSpeed: Int = 0,
    val defaultAccel: Int = 0,
    val defaultBoostMap: Int = 105, // kPa (100 - 300 kPa)
    val defaultMaf: Int = 18,       // g/s (0 - 500 g/s)
    val defaultCamSync: Boolean = true,
    val defaultInjectorPulse: Boolean = true
)

object VehicleCatalog {
    val allVehicles: List<VehicleModel> = listOf(
        // ==================== MAHINDRA ====================
        VehicleModel(
            id = "mahindra_bolero",
            brand = "MAHINDRA",
            modelName = "BOLERO",
            category = "LCV / SUV",
            ecuSystem = "Bosch EDC17C53 / EDC17C63",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 320,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 110,
            defaultMaf = 20
        ),
        VehicleModel(
            id = "mahindra_scorpio",
            brand = "MAHINDRA",
            modelName = "SCORPIO / SCORPIO-N",
            category = "SUV",
            ecuSystem = "Bosch EDC17C53 / MD1CS018 mHawk",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 350,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 115,
            defaultMaf = 24
        ),
        VehicleModel(
            id = "mahindra_pickup",
            brand = "MAHINDRA",
            modelName = "PICKUP (MAXX / FB)",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 Bolero Pik-Up",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 300,
            defaultEct = 85,
            defaultSpeed = 0,
            defaultBoostMap = 108,
            defaultMaf = 19
        ),
        VehicleModel(
            id = "mahindra_supro",
            brand = "MAHINDRA",
            modelName = "SUPRO (PROFIT TRUCK / MAXI)",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 Direct Injection",
            crankPattern = "60-2",
            defaultRpm = 850,
            defaultRail = 300,
            defaultEct = 85,
            defaultSpeed = 0,
            defaultBoostMap = 105,
            defaultMaf = 15
        ),
        VehicleModel(
            id = "mahindra_jeeto",
            brand = "MAHINDRA",
            modelName = "JEETO (PLUS / STRONG)",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 Single Cylinder",
            crankPattern = "60-2",
            defaultRpm = 900,
            defaultRail = 280,
            defaultEct = 85,
            defaultSpeed = 0,
            defaultBoostMap = 102,
            defaultMaf = 12
        ),
        VehicleModel(
            id = "mahindra_xuv_thar",
            brand = "MAHINDRA",
            modelName = "THAR / XUV500 / XUV700",
            category = "SUV",
            ecuSystem = "Bosch EDC17C53 / Delphi DCM2.7",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 340,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 120,
            defaultMaf = 26
        ),

        // ==================== TATA ====================
        VehicleModel(
            id = "tata_407",
            brand = "TATA",
            modelName = "TATA 407",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV54 / Delphi CRDI",
            crankPattern = "60-2",
            defaultRpm = 750,
            defaultRail = 350,
            defaultEct = 85,
            defaultSpeed = 0,
            defaultBoostMap = 120,
            defaultMaf = 28
        ),
        VehicleModel(
            id = "tata_1109",
            brand = "TATA",
            modelName = "TATA 1109",
            category = "HCV",
            ecuSystem = "Bosch EDC17CV54 / Cummins ISBe",
            crankPattern = "60-2",
            defaultRpm = 750,
            defaultRail = 400,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 135,
            defaultMaf = 38
        ),
        VehicleModel(
            id = "tata_909",
            brand = "TATA",
            modelName = "TATA 909",
            category = "LCV / HCV",
            ecuSystem = "Bosch EDC17CV54 Common Rail",
            crankPattern = "60-2",
            defaultRpm = 750,
            defaultRail = 380,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 130,
            defaultMaf = 34
        ),
        VehicleModel(
            id = "tata_all",
            brand = "TATA",
            modelName = "ALL TATA",
            category = "MULTI-VEHICLE",
            ecuSystem = "Universal Tata CRDI Test Profile",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 320,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 115,
            defaultMaf = 22
        ),
        VehicleModel(
            id = "tata_nano",
            brand = "TATA",
            modelName = "TATA NANO",
            category = "CAR",
            ecuSystem = "Bosch ValueMotronic EMS / MPFI",
            crankPattern = "36-2",
            defaultRpm = 950,
            defaultRail = 250,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 100,
            defaultMaf = 10
        ),
        VehicleModel(
            id = "tata_ace_gold",
            brand = "TATA",
            modelName = "TATA ACE GOLD / INTRA V10-V50",
            category = "LCV",
            ecuSystem = "Bosch / Delphi Dicor CRDI",
            crankPattern = "60-2",
            defaultRpm = 850,
            defaultRail = 300,
            defaultEct = 85,
            defaultSpeed = 0,
            defaultBoostMap = 108,
            defaultMaf = 16
        ),
        VehicleModel(
            id = "tata_safari_nexon",
            brand = "TATA",
            modelName = "SAFARI / HARRIER / NEXON",
            category = "CAR / SUV",
            ecuSystem = "Bosch EDC17C69 / MD1CS018 Kryotec",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 320,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 118,
            defaultMaf = 24
        ),

        // ==================== ASHOK LEYLAND ====================
        VehicleModel(
            id = "ashok_leyland_dost",
            brand = "ASHOK LEYLAND",
            modelName = "DOST / DOST+",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 1.5L TDCR",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 320,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 112,
            defaultMaf = 20
        ),
        VehicleModel(
            id = "ashok_leyland_bada_dost",
            brand = "ASHOK LEYLAND",
            modelName = "BADA DOST (i3 / i4)",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 BS6 P15",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 350,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 118,
            defaultMaf = 22
        ),
        VehicleModel(
            id = "ashok_leyland_saathi",
            brand = "ASHOK LEYLAND",
            modelName = "SAATHI",
            category = "LCV",
            ecuSystem = "Bosch CRDI LCV Bench Profile",
            crankPattern = "60-2",
            defaultRpm = 850,
            defaultRail = 300,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 110,
            defaultMaf = 18
        ),
        VehicleModel(
            id = "ashok_leyland_partner_ekomet",
            brand = "ASHOK LEYLAND",
            modelName = "PARTNER / ECOMET / VIKING",
            category = "HCV / BUS",
            ecuSystem = "Bosch EDC17CV54 / H-Series CRS",
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 420,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 140,
            defaultMaf = 42
        ),

        // ==================== MARUTI SUZUKI ====================
        VehicleModel(
            id = "maruti_swift_dzire_ddis",
            brand = "MARUTI SUZUKI",
            modelName = "SWIFT / DZIRE / ERTIGA 1.3 DDiS",
            category = "CAR",
            ecuSystem = "Magneti Marelli MJD 8F3 / Bosch EDC16C39",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 300,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 108,
            defaultMaf = 17
        ),
        VehicleModel(
            id = "maruti_brezza_scross",
            brand = "MARUTI SUZUKI",
            modelName = "BREZZA / S-CROSS / CIAZ DDiS",
            category = "CAR / SUV",
            ecuSystem = "Bosch EDC17C49 / EDC17C69",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 320,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 112,
            defaultMaf = 19
        ),
        VehicleModel(
            id = "maruti_super_carry_eeco",
            brand = "MARUTI SUZUKI",
            modelName = "SUPER CARRY / EECO / ALTO K-SERIES",
            category = "LCV / CAR",
            ecuSystem = "Bosch ME17.9.64 / Denso / EDC17C63",
            crankPattern = "60-2",
            defaultRpm = 850,
            defaultRail = 280,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 102,
            defaultMaf = 14
        ),

        // ==================== HYUNDAI ====================
        VehicleModel(
            id = "hyundai_creta_verna_crdi",
            brand = "HYUNDAI",
            modelName = "CRETA / VERNA / VENUE U2 CRDi",
            category = "CAR / SUV",
            ecuSystem = "Bosch EDC17C57 / MD1CS012",
            crankPattern = "60-2",
            defaultRpm = 780,
            defaultRail = 330,
            defaultEct = 90,
            defaultSpeed = 0,
            defaultBoostMap = 115,
            defaultMaf = 21
        ),
        VehicleModel(
            id = "hyundai_i20_grand_i10",
            brand = "HYUNDAI",
            modelName = "i20 / GRAND i10 / XCENT CRDi",
            category = "CAR",
            ecuSystem = "Bosch EDC17C53 / Delphi DCM3.7",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 310,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 110,
            defaultMaf = 18
        ),

        // ==================== FORCE MOTORS ====================
        VehicleModel(
            id = "force_traveller_urbania",
            brand = "FORCE",
            modelName = "TRAVELLER / URBANIA / SHAKTIMAN",
            category = "VAN / BUS",
            ecuSystem = "Bosch EDC17C53 / EDC17C63 FM2.6 CR",
            crankPattern = "60-2",
            defaultRpm = 780,
            defaultRail = 350,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 122,
            defaultMaf = 28
        ),
        VehicleModel(
            id = "force_gurkha_trax",
            brand = "FORCE",
            modelName = "GURKHA / TRAX CRUISER / TOOFAN",
            category = "SUV / MUV",
            ecuSystem = "Bosch EDC17C53 Mercedes OM616 CRDI",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 330,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 115,
            defaultMaf = 24
        ),

        // ==================== EICHER ====================
        VehicleModel(
            id = "eicher_pro_2000_3000",
            brand = "EICHER",
            modelName = "PRO 2049 / PRO 2095 / PRO 3015",
            category = "LCV / HCV",
            ecuSystem = "Bosch EDC17CV54 / VECV E494 CRS",
            crankPattern = "60-2",
            defaultRpm = 750,
            defaultRail = 400,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 135,
            defaultMaf = 38
        ),
        VehicleModel(
            id = "eicher_pro_6000_starline",
            brand = "EICHER",
            modelName = "PRO 6000 / 10.90 / STARLINE BUS",
            category = "HCV / BUS",
            ecuSystem = "Bosch EDC17CV41 / EMS 2.2 VECV",
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 450,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 145,
            defaultMaf = 45
        ),

        // ==================== BHARATBENZ ====================
        VehicleModel(
            id = "bharatbenz_1015_1217_1617",
            brand = "BHARATBENZ",
            modelName = "1015R / 1217C / 1617R (4D34i)",
            category = "HCV",
            ecuSystem = "Bosch EDC17CV41 / ACM2.1 Daimler",
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 440,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 140,
            defaultMaf = 44
        ),
        VehicleModel(
            id = "bharatbenz_2823_3528_5528",
            brand = "BHARATBENZ",
            modelName = "2823C / 3528C / 5528TT (OM926)",
            category = "HCV",
            ecuSystem = "Continental MCM2.1 / Bosch Heavy Duty",
            crankPattern = "60-2",
            defaultRpm = 680,
            defaultRail = 480,
            defaultEct = 88,
            defaultSpeed = 0,
            defaultBoostMap = 150,
            defaultMaf = 52
        )
    )

    val brands = listOf(
        "ALL",
        "MAHINDRA",
        "TATA",
        "ASHOK LEYLAND",
        "MARUTI SUZUKI",
        "HYUNDAI",
        "FORCE",
        "EICHER",
        "BHARATBENZ"
    )

    fun findById(id: String): VehicleModel {
        return allVehicles.find { it.id == id } ?: allVehicles.first()
    }
}

@Entity(tableName = "ecu_presets")
data class EcuPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val presetName: String,
    val vehicleId: String,
    val brand: String,
    val modelName: String,
    val rpm: Int,
    val railPressure: Int,
    val ectTemp: Int,
    val vehicleSpeed: Int,
    val accelerator: Int,
    val boostMapKpa: Int = 105,
    val mafGramsSec: Int = 18,
    val camSyncEnabled: Boolean = true,
    val injectorPulseEnabled: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface EcuPresetDao {
    @Query("SELECT * FROM ecu_presets ORDER BY timestamp DESC")
    fun getAllPresets(): Flow<List<EcuPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EcuPresetEntity)

    @Query("DELETE FROM ecu_presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)
}

@Database(entities = [EcuPresetEntity::class], version = 2, exportSchema = false)
abstract class EcuLabDatabase : RoomDatabase() {
    abstract fun ecuPresetDao(): EcuPresetDao
}

class EcuPresetRepository(private val dao: EcuPresetDao) {
    val allPresets: Flow<List<EcuPresetEntity>> = dao.getAllPresets()

    suspend fun savePreset(preset: EcuPresetEntity) {
        dao.insertPreset(preset)
    }

    suspend fun deletePreset(id: Int) {
        dao.deletePresetById(id)
    }
}
