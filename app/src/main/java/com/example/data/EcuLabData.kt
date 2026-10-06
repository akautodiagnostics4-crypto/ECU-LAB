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
    val category: String, // CAR, LCV, HCV, BUS
    val ecuSystem: String,
    internal val crankPattern: String, // Hidden from display! Used only for Arduino HC-05 packet
    val defaultRpm: Int = 800,
    val defaultRail: Int = 300,
    val defaultEct: Int = 90,
    val defaultSpeed: Int = 0,
    val defaultAccel: Int = 0
)

object VehicleCatalog {
    val allVehicles: List<VehicleModel> = listOf(
        // MAHINDRA (Using 60-2 tooth pattern internally)
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
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "mahindra_scorpio",
            brand = "MAHINDRA",
            modelName = "SCORPIO",
            category = "SUV",
            ecuSystem = "Bosch EDC17C53 mHawk",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 350,
            defaultEct = 90,
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "mahindra_pickup",
            brand = "MAHINDRA",
            modelName = "PICKUP",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 Bolero Pik-Up",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 300,
            defaultEct = 85,
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "mahindra_supro",
            brand = "MAHINDRA",
            modelName = "SUPRO",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 Direct Injection",
            crankPattern = "60-2",
            defaultRpm = 850,
            defaultRail = 300,
            defaultEct = 85,
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "mahindra_jeeto",
            brand = "MAHINDRA",
            modelName = "JEETO",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 Single Cylinder",
            crankPattern = "60-2",
            defaultRpm = 900,
            defaultRail = 280,
            defaultEct = 85,
            defaultSpeed = 0
        ),

        // TATA (407, 1109, 909, ALL TATA using 60-2, TATA NANO using 36-2 internally)
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
            defaultSpeed = 0
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
            defaultSpeed = 0
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
            defaultSpeed = 0
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
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "tata_nano",
            brand = "TATA",
            modelName = "TATA NANO",
            category = "CAR",
            ecuSystem = "Bosch Valueronic EMS / MPFI",
            crankPattern = "36-2",
            defaultRpm = 950,
            defaultRail = 250,
            defaultEct = 90,
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "tata_ace_gold",
            brand = "TATA",
            modelName = "TATA ACE / INTRA",
            category = "LCV",
            ecuSystem = "Bosch / Delphi Dicor CRDI",
            crankPattern = "60-2",
            defaultRpm = 850,
            defaultRail = 300,
            defaultEct = 85,
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "tata_safari_nexon",
            brand = "TATA",
            modelName = "SAFARI / NEXON / TIAGO",
            category = "CAR / SUV",
            ecuSystem = "Bosch EDC17C69 Revotorq",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 320,
            defaultEct = 90,
            defaultSpeed = 0
        ),

        // ASHOK LEYLAND (DOST, BADA DOST, SAATHI using 60-2 internally)
        VehicleModel(
            id = "ashok_leyland_dost",
            brand = "ASHOK LEYLAND",
            modelName = "DOST",
            category = "LCV",
            ecuSystem = "Bosch EDC17C53 1.5L TDCR",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 320,
            defaultEct = 88,
            defaultSpeed = 0
        ),
        VehicleModel(
            id = "ashok_leyland_bada_dost",
            brand = "ASHOK LEYLAND",
            modelName = "BADA DOST",
            category = "LCV",
            ecuSystem = "Bosch EDC17C63 BS6 P15",
            crankPattern = "60-2",
            defaultRpm = 800,
            defaultRail = 350,
            defaultEct = 90,
            defaultSpeed = 0
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
            defaultSpeed = 0
        )
    )

    val brands = listOf("ALL", "MAHINDRA", "TATA", "ASHOK LEYLAND")

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

@Database(entities = [EcuPresetEntity::class], version = 1, exportSchema = false)
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
