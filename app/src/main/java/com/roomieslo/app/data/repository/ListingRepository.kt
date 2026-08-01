package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.ListingDto
import com.roomieslo.app.data.remote.dto.NewListingDto
import com.roomieslo.app.domain.model.Listing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import com.roomieslo.app.data.local.dao.ListingDao
import com.roomieslo.app.data.local.entity.toDomain
import com.roomieslo.app.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * F06-F10, F19: oglasi za sobe ter iskanje po lokaciji in proracunu.
 *
 * Filtriranje in razvrscanje se izvajata na strezniku prek PostgREST
 * (npr. ?is_filled=eq.false&price_per_month=lte.400&order=created_at.desc).
 */
@Singleton
class ListingRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    private val listingDao : ListingDao
) {
    /** Branje iz predpomnilnika -- to opazuje UI, ne omrezja. */
    fun observeListings(location: String, maxPrice: Double): Flow<List<Listing>> =
        listingDao.observeListings(location, maxPrice)
            .map {rows -> rows.map {it.toDomain() } }

    /** F07: vsi nezasedeni oglasi iz predpomnilnika, brez filtrov. */
    fun observeAllListings(): Flow<List<Listing>> =
        listingDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    /**
     * F07: polna sinhronizacija brez filtrov. Streznik je merodajen za vse nezasedene
     * oglase, zato izbrisani oglasi iz predpomnilnika izginejo takoj.
     */
    suspend fun syncAllFromRemote() {
        val now = System.currentTimeMillis()
        val fresh = getListings()
        listingDao.replaceAll(fresh.map { it.toEntity(now) })
    }

    /**
     * Osvezi predpomnilnik za dano iskanje. Ker je poizvedba filtrirana, zapisov, ki
     * filtru ne ustrezajo, ne smemo brisati -- odstranijo se sele po zastaranju.
     */
    suspend fun syncFromRemote(location: String? = null, maxPrice: Double? = null) {
        val now = System.currentTimeMillis()
        val fresh = getListings(location, maxPrice)
        listingDao.syncListings(fresh.map { it.toEntity(now) }, cutoff = now - STALE_AFTER_MS)
    }

    /** F07/F10/F11: seznam oglasov (samo nezasedeni), z izbirnim filtrom lokacije in cene. */
    suspend fun getListings(location: String? = null, maxPrice: Double? = null): List<Listing> =
        supabase.from("listings").select {
            filter {
                eq("is_filled", false)
                if (!location.isNullOrBlank()) ilike("location", "%${location.trim()}%")
                if (maxPrice != null) lte("price_per_month", maxPrice)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<ListingDto>().map { it.toDomain() }

    /** F08: podrobnosti posameznega oglasa. */
    suspend fun getListing(id: String): Listing? =
        supabase.from("listings").select { filter { eq("id", id) } }
            .decodeSingleOrNull<ListingDto>()?.toDomain()

    /** F06: ustvari nov oglas (owner_id = trenutni uporabnik). */
    suspend fun createListing(location: String, pricePerMonth: Double, description: String) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("listings").insert(
            NewListingDto(ownerId = uid, location = location.trim(), pricePerMonth = pricePerMonth, description = description.trim())
        )
    }

    /** F19: uredi obstojeci oglas. */
    suspend fun updateListing(id: String, location: String, pricePerMonth: Double, description: String) {
        supabase.from("listings").update({
            set("location", location.trim())
            set("price_per_month", pricePerMonth)
            set("description", description.trim())
        }) { filter { eq("id", id) } }
    }

    /** F09: oznaci oglas kot zaseden/na voljo. */
    suspend fun markFilled(id: String, filled: Boolean) {
        supabase.from("listings").update({ set("is_filled", filled) }) { filter { eq("id", id) } }
    }

    /** F19: izbrisi oglas. */
    suspend fun deleteListing(id: String) {
        supabase.from("listings").delete { filter { eq("id", id) } }
    }

    /** Ali je trenutni uporabnik lastnik danega oglasa (za prikaz gumbov urejanja). */
    fun currentUserId(): String? = authRepository.currentUserId()

    companion object {
        /** Zapisi, ki jih sinhronizacija ni osvezila 7 dni, se odstranijo iz predpomnilnika. */
        private const val STALE_AFTER_MS = 7L * 24 * 60 * 60 * 1000
    }
}
