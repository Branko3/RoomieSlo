package com.roomieslo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.roomieslo.app.data.local.entity.ListingEntity
import com.roomieslo.app.data.local.entity.MatchEntity
import com.roomieslo.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE isAvailable = 1")
    fun observeAvailableProfiles(): Flow<List<ProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(profiles: List<ProfileEntity>)
}

@Dao
interface ListingDao {
    // F10: indeksirana poizvedba po lokaciji in ceni
    @Query("SELECT * FROM listings WHERE isFilled = 0 AND location LIKE '%' || :location || '%' AND pricePerMonth <= :maxPrice")
    fun observeListings(location: String, maxPrice: Double): Flow<List<ListingEntity>>

    /** F07: vsi nezasedeni oglasi iz predpomnilnika, brez filtrov (seznam oglasov). */
    @Query("SELECT * FROM listings WHERE isFilled = 0")
    fun observeAll(): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(listings: List<ListingEntity>)

    /** Odstrani zapise, ki jih streznik ze dolgo ni vec vrnil (izbrisani ali zasedeni oglasi). */
    @Query("DELETE FROM listings WHERE lastSyncedAt < :cutoff")
    suspend fun deleteStale(cutoff: Long)

    /** Vpis in ciscenje v eni transakciji, da UI ne vidi vmesnega stanja. */
    @Transaction
    suspend fun syncListings(fresh: List<ListingEntity>, cutoff: Long) {
        upsertAll(fresh)
        deleteStale(cutoff)
    }

    @Query("DELETE FROM listings")
    suspend fun deleteAll()

    @Query("DELETE FROM listings WHERE id NOT IN (:ohranjeniIds)")
    suspend fun deleteNotIn(ohranjeniIds: List<String>)

    /**
     * Polna sinhronizacija: pri poizvedbi brez filtrov je streznik merodajen za vse
     * nezasedene oglase, zato zapise, ki jih ni vrnil, odstranimo takoj -- brisan
     * oglas tako ne ostane v predpomnilniku do zastaranja.
     */
    @Transaction
    suspend fun replaceAll(fresh: List<ListingEntity>) {
        if (fresh.isEmpty()) {
            deleteAll()
        } else {
            upsertAll(fresh)
            deleteNotIn(fresh.map { it.id })
        }
    }
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE userIdA = :userId OR userIdB = :userId")
    fun observeMatchesForUser(userId: String): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(matches: List<MatchEntity>)
}
