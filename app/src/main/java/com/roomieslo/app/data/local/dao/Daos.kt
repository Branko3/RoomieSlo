package com.roomieslo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(listings: List<ListingEntity>)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE userIdA = :userId OR userIdB = :userId")
    fun observeMatchesForUser(userId: String): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(matches: List<MatchEntity>)
}
