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

enum class VehicleCategoryType(
    val title: String,
    val subtitle: String,
    val isComingSoon: Boolean
) {
    CAR("CAR", "Passenger Cars & SUVs", true),
    LCV("LCV", "Light Commercial Vehicles", false),
    HCV("HCV", "Heavy Commercial Vehicles", true),
    BUS("BUS", "Passenger & Coach Buses", true)
}

enum class VehicleVisualType {
    MINI_TRUCK,
    PICKUP,
    VAN,
    COMMERCIAL_TRUCK,
    SUV
}

enum class StimulableSensor(
    val id: String,
    val title: String,
    val shortLabel: String,
    val unit: String,
    val minValue: Int,
    val maxValue: Int,
    val step: Int,
    val defaultValue: Int
) {
    RAIL_PRESSURE(
        id = "rail",
        title = "Rail Pressure",
        shortLabel = "RAIL",
        unit = "bar",
        minValue = 0,
        maxValue = 1200,
        step = 10,
        defaultValue = 350
    ),
    COOLANT_TEMP(
        id = "ect",
        title = "Coolant Temp",
        shortLabel = "ECT",
        unit = "°C",
        minValue = 0,
        maxValue = 125,
        step = 1,
        defaultValue = 88
    ),
    VEHICLE_SPEED(
        id = "speed",
        title = "Vehicle Speed",
        shortLabel = "VSS",
        unit = "km/h",
        minValue = 0,
        maxValue = 220,
        step = 2,
        defaultValue = 40
    ),
    BOOST_PRESSURE(
        id = "map",
        title = "Boost Pressure Sensor",
        shortLabel = "MAP",
        unit = "kPa",
        minValue = 100,
        maxValue = 300,
        step = 5,
        defaultValue = 130
    ),
    AIR_MASS(
        id = "maf",
        title = "Air Mass Sensor",
        shortLabel = "MAF",
        unit = "g/s",
        minValue = 0,
        maxValue = 500,
        step = 5,
        defaultValue = 35
    )
}

/**
 * Represents a Vehicle Profile in AK ECU STIMULATOR.
 * [crankPattern] is kept strictly internal for Bluetooth hardware transmission and is NEVER displayed on the UI.
 */
data class VehicleModel(
    val id: String,
    val brand: String,
    val modelName: String,
    val category: String,
    val ecuSystem: String,
    val visualType: VehicleVisualType = VehicleVisualType.COMMERCIAL_TRUCK,
    internal val crankPattern: String = "60-2",
    val defaultRpm: Int = 700,
    val defaultRail: Int = 350,
    val defaultEct: Int = 88,
    val defaultSpeed: Int = 0,
    val defaultAccel: Int = 0,
    val defaultBoostMap: Int = 110,
    val defaultMaf: Int = 20,
    val defaultCamSync: Boolean = true,
    val defaultInjectorPulse: Boolean = true
)

object VehicleCatalog {

    val lcvBrands = listOf(
        "TATA",
        "MAHINDRA",
        "ASHOK LEYLAND",
        "FORCE",
        "BHARATBENZ"
    )

    val comingSoonBrandsByCategory: Map<VehicleCategoryType, List<String>> = mapOf(
        VehicleCategoryType.CAR to listOf(
            "MARUTI SUZUKI",
            "HYUNDAI",
            "TATA",
            "MAHINDRA",
            "HONDA",
            "TOYOTA",
            "KIA",
            "VOLKSWAGEN"
        ),
        VehicleCategoryType.HCV to listOf(
            "TATA",
            "ASHOK LEYLAND",
            "EICHER",
            "BHARATBENZ",
            "MAHINDRA",
            "VOLVO"
        ),
        VehicleCategoryType.BUS to listOf(
            "TATA",
            "ASHOK LEYLAND",
            "EICHER",
            "BHARATBENZ",
            "FORCE",
            "VOLVO"
        )
    )

    val allVehicles: List<VehicleModel> = listOf(
        // ==================== TATA LCV ====================
        VehicleModel(
            id = "tata_ace_gold",
            brand = "TATA",
            modelName = "Ace Gold",
            category = "LCV",
            ecuSystem = "Bosch / Delphi CRDI SCV",
            visualType = VehicleVisualType.MINI_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 85,
            defaultBoostMap = 108,
            defaultMaf = 16
        ),
        VehicleModel(
            id = "tata_intra_v30_v50",
            brand = "TATA",
            modelName = "Intra V30 / V50",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 DI CRDI",
            visualType = VehicleVisualType.MINI_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 86,
            defaultBoostMap = 112,
            defaultMaf = 20
        ),
        VehicleModel(
            id = "tata_yodha",
            brand = "TATA",
            modelName = "Yodha Pickup",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 / EDC17CV54",
            visualType = VehicleVisualType.PICKUP,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 118,
            defaultMaf = 24
        ),
        VehicleModel(
            id = "tata_407_gold",
            brand = "TATA",
            modelName = "407 Gold SFC",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV54 / 4SPCR",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 122,
            defaultMaf = 28
        ),
        VehicleModel(
            id = "tata_709_ultra",
            brand = "TATA",
            modelName = "709g / Ultra T.7",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV54 Common Rail",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 130,
            defaultMaf = 32
        ),

        // ==================== MAHINDRA LCV ====================
        VehicleModel(
            id = "mahindra_bolero_pickup",
            brand = "MAHINDRA",
            modelName = "Bolero Pik-Up",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 m2DiCR",
            visualType = VehicleVisualType.PICKUP,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 115,
            defaultMaf = 22
        ),
        VehicleModel(
            id = "mahindra_bolero_camper",
            brand = "MAHINDRA",
            modelName = "Bolero Power+ / Camper",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 / EDC17C63",
            visualType = VehicleVisualType.SUV,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 115,
            defaultMaf = 22
        ),
        VehicleModel(
            id = "mahindra_supro",
            brand = "MAHINDRA",
            modelName = "Supro Profit Truck",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 Direct Injection",
            visualType = VehicleVisualType.MINI_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 85,
            defaultBoostMap = 108,
            defaultMaf = 16
        ),
        VehicleModel(
            id = "mahindra_jeeto",
            brand = "MAHINDRA",
            modelName = "Jeeto Plus / Strong",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 Single Cylinder",
            visualType = VehicleVisualType.MINI_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 85,
            defaultBoostMap = 104,
            defaultMaf = 12
        ),
        VehicleModel(
            id = "mahindra_maxximo",
            brand = "MAHINDRA",
            modelName = "Maxximo Plus",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 / C2 CRDI",
            visualType = VehicleVisualType.MINI_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 86,
            defaultBoostMap = 108,
            defaultMaf = 16
        ),
        VehicleModel(
            id = "mahindra_loadking_optimo",
            brand = "MAHINDRA",
            modelName = "Loadking Optimo",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV54 CRDI",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 122,
            defaultMaf = 26
        ),
        VehicleModel(
            id = "mahindra_furio_7",
            brand = "MAHINDRA",
            modelName = "Furio 7 / JAYO",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV54 mDi Tech",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 126,
            defaultMaf = 30
        ),

        // ==================== ASHOK LEYLAND LCV ====================
        VehicleModel(
            id = "ashok_leyland_dost",
            brand = "ASHOK LEYLAND",
            modelName = "Dost+ / Dost Strong",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 1.5L TDCR",
            visualType = VehicleVisualType.MINI_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 112,
            defaultMaf = 20
        ),
        VehicleModel(
            id = "ashok_leyland_bada_dost",
            brand = "ASHOK LEYLAND",
            modelName = "Bada Dost i3 / i4",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 BS6 P15",
            visualType = VehicleVisualType.PICKUP,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 118,
            defaultMaf = 24
        ),
        VehicleModel(
            id = "ashok_leyland_saathi",
            brand = "ASHOK LEYLAND",
            modelName = "Saathi",
            category = "LCV",
            ecuSystem = "Bosch CRDI LCV Profile",
            visualType = VehicleVisualType.MINI_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 86,
            defaultBoostMap = 110,
            defaultMaf = 18
        ),
        VehicleModel(
            id = "ashok_leyland_partner",
            brand = "ASHOK LEYLAND",
            modelName = "Partner 4T / 6T",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV54 ZD30 DDTi",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 128,
            defaultMaf = 32
        ),

        // ==================== FORCE LCV ====================
        VehicleModel(
            id = "force_traveller",
            brand = "FORCE",
            modelName = "Traveller Delivery Van",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 FM 2.6 CR",
            visualType = VehicleVisualType.VAN,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 122,
            defaultMaf = 28
        ),
        VehicleModel(
            id = "force_urbania",
            brand = "FORCE",
            modelName = "Urbania",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 FM 2.6 CRDI",
            visualType = VehicleVisualType.VAN,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 124,
            defaultMaf = 28
        ),
        VehicleModel(
            id = "force_trax_cargo",
            brand = "FORCE",
            modelName = "Trax Cruiser / Cargo",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 OM616 CR",
            visualType = VehicleVisualType.SUV,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 116,
            defaultMaf = 24
        ),
        VehicleModel(
            id = "force_shaktiman",
            brand = "FORCE",
            modelName = "Shaktiman 400",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV54 Common Rail",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 125,
            defaultMaf = 30
        ),

        // ==================== BHARATBENZ LCV ====================
        VehicleModel(
            id = "bharatbenz_1015r",
            brand = "BHARATBENZ",
            modelName = "1015R",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV41 / 4D34i",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 135,
            defaultMaf = 38
        ),
        VehicleModel(
            id = "bharatbenz_1117r",
            brand = "BHARATBENZ",
            modelName = "1117R",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV41 / Daimler LDT",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 138,
            defaultMaf = 40
        ),
        VehicleModel(
            id = "bharatbenz_1217c",
            brand = "BHARATBENZ",
            modelName = "1217C",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV41 / ACM2.1",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 140,
            defaultMaf = 42
        ),
        VehicleModel(
            id = "bharatbenz_1617r",
            brand = "BHARATBENZ",
            modelName = "1617R",
            category = "LCV",
            ecuSystem = "Bosch EDC17CV41 / 4D34i",
            visualType = VehicleVisualType.COMMERCIAL_TRUCK,
            crankPattern = "60-2",
            defaultRpm = 700,
            defaultRail = 350,
            defaultEct = 88,
            defaultBoostMap = 142,
            defaultMaf = 44
        )
    )

    fun vehiclesForLcvBrand(brand: String): List<VehicleModel> {
        return allVehicles.filter { it.brand.equals(brand, ignoreCase = true) }
    }

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

@Entity(tableName = "stimulation_history")
data class StimulationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modeType: String,
    val vehicleLabel: String,
    val summary: String,
    val rpm: Int,
    val railPressure: Int,
    val sensorCount: Int,
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

@Dao
interface StimulationHistoryDao {
    @Query("SELECT * FROM stimulation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<StimulationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: StimulationHistoryEntity)

    @Query("DELETE FROM stimulation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM stimulation_history")
    suspend fun clearAllHistory()
}

@Database(
    entities = [EcuPresetEntity::class, StimulationHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class EcuLabDatabase : RoomDatabase() {
    abstract fun ecuPresetDao(): EcuPresetDao
    abstract fun stimulationHistoryDao(): StimulationHistoryDao
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

class StimulationHistoryRepository(private val dao: StimulationHistoryDao) {
    val allHistory: Flow<List<StimulationHistoryEntity>> = dao.getAllHistory()

    suspend fun addEntry(entry: StimulationHistoryEntity) {
        dao.insertHistory(entry)
    }

    suspend fun deleteEntry(id: Int) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearAll() {
        dao.clearAllHistory()
    }
}
