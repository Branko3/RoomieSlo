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
    /**
     * F10: iskanje po lokaciji in ceni.
     *
     * Mejo `limit` doloca zaslon: ko uporabnik pride do konca seznama, jo poveca in
     * Room sam odda sirsi nabor. Tako se ob vsakem branju ne prenese cel predpomnilnik.
     * Pogoja `isFilled` in `pricePerMonth` pokriva indeks (isFilled, pricePerMonth),
     * razvrscanje pa indeks (isFilled, createdAt).
     *
     * Vodilni `%` pri LIKE prepreci uporabo indeksa, zato SQLite lokacije pregleda
     * zaporedno. To je zavestna odlocitev: `limit` pregled omeji na eno stran,
     * polnotekstovno iskanje (@Fts4) pa bi ujemalo po besedah in predponah namesto
     * po poljubnem podnizu -- "ubljan" ne bi vec naslo "Ljubljana".
     */
    @Query(
        """
        SELECT * FROM listings
        WHERE isFilled = 0
          AND location LIKE '%' || :location || '%'
          AND pricePerMonth <= :maxPrice
        ORDER BY createdAt DESC
        LIMIT :limit
        """
    )
    fun observeListings(location: String, maxPrice: Double, limit: Int): Flow<List<ListingEntity>>

    /** F07: nezasedeni oglasi brez filtrov, po straneh (seznam oglasov). */
    @Query("SELECT * FROM listings WHERE isFilled = 0 ORDER BY createdAt DESC LIMIT :limit")
    fun observeAll(limit: Int): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(listings: List<ListingEntity>)

    /**
     * Odstrani zapise, starejse od dane meje.
     *
     * Uporablja se na dva nacina: z mejo "zdaj minus 7 dni" za zastarele zapise, in z
     * oznako obhoda po straneh -- takrat imajo vsi zapisi tega obhoda enak lastSyncedAt,
     * zato ta klic pobrise natanko tiste, ki jih streznik v obhodu ni vec vrnil.
     */
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
