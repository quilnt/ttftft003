package com.example.core.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.core.model.CompTier
import kotlinx.coroutines.flow.Flow
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "meta_compositions")
data class MetaCompEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tier: String, // "S_PLUS", "S", "A", "B", "C"
    val patchVersion: String,
    val carryChampionName: String,
    val tankChampionName: String,
    val championsJson: String, // List<ChampionRef> serialized as JSON
    val traitsJson: String, // List<Pair<String, Int>> as JSON
    val augmentsJson: String, // List<String> as JSON
    val playstyle: String,
    val rollLevel: Int,
    val difficulty: String,
    val earlyGameGuide: String,
    val midGameGuide: String,
    val lateGameGuide: String,
    val prosJson: String,
    val consJson: String,
    val isFavorite: Boolean = false
)

@Entity(tableName = "champions")
data class ChampionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cost: Int,
    val traitsJson: String,
    val recommendedItemsJson: String,
    val stats: String,
    val description: String
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val componentsJson: String,
    val description: String,
    val category: String,
    val bestHoldersJson: String
)

@Entity(tableName = "match_history")
data class MatchEntity(
    @PrimaryKey val matchId: String,
    val gameMode: String,
    val gameDurationMinutes: Int,
    val placement: Int,
    val playedAtTimestamp: Long,
    val mainCarry: String,
    val primaryTraitsJson: String,
    val championsJson: String
)

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listStringAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return listStringAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            listStringAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Dao
interface MetaCompDao {
    @Query("SELECT * FROM meta_compositions ORDER BY tier ASC, name ASC")
    fun getAllCompositions(): Flow<List<MetaCompEntity>>

    @Query("SELECT * FROM meta_compositions WHERE isFavorite = 1")
    fun getFavoriteCompositions(): Flow<List<MetaCompEntity>>

    @Query("SELECT * FROM meta_compositions WHERE id = :id LIMIT 1")
    suspend fun getCompById(id: String): MetaCompEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comps: List<MetaCompEntity>)

    @Query("UPDATE meta_compositions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM meta_compositions")
    suspend fun clearAll()
}

@Dao
interface ChampionDao {
    @Query("SELECT * FROM champions ORDER BY cost ASC, name ASC")
    fun getAllChampions(): Flow<List<ChampionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(champions: List<ChampionEntity>)
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY category ASC, name ASC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM match_history ORDER BY playedAtTimestamp DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchEntity>)

    @Query("DELETE FROM match_history")
    suspend fun clearMatches()
}

@Database(
    entities = [MetaCompEntity::class, ChampionEntity::class, ItemEntity::class, MatchEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metaCompDao(): MetaCompDao
    abstract fun championDao(): ChampionDao
    abstract fun itemDao(): ItemDao
    abstract fun matchDao(): MatchDao
}
