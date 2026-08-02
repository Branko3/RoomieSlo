package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.ListingDto
import com.roomieslo.app.data.remote.dto.NewListingDto
import com.roomieslo.app.domain.model.Listing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import com.roomieslo.app.data.local.dao.ListingDao
import com.roomieslo.app.data.local.entity.toDomain
import com.roomieslo.app.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Izid sinhronizacije ene strani.
 *
 * @param zadnjiCreatedAt kazalec za naslednjo stran (created_at zadnjega prejetega oglasa)
 * @param jeKonec streznik je vrnil manj vrstic, kot smo jih zahtevali -- nabor je izcrpan
 */
data class StranOglasov(
    val steviloPrejetih: Int,
    val zadnjiCreatedAt: String?,
    val jeKonec: Boolean
)

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
    fun observeListings(location: String, maxPrice: Double, limit: Int): Flow<List<Listing>> =
        listingDao.observeListings(location, maxPrice, limit)
            .map { rows -> rows.map { it.toDomain() } }

    /** F07: vsi nezasedeni oglasi iz predpomnilnika, brez filtrov. */
    fun observeAllListings(limit: Int): Flow<List<Listing>> =
        listingDao.observeAll(limit).map { rows -> rows.map { it.toDomain() } }

    /**
     * F07: ena stran seznama oglasov (brez filtrov).
     *
     * Vsi klici enega obhoda uporabijo isto `oznaka`, zato lahko ob koncu nabora
     * pobrisemo vse, cesar obhod ni osvezil -- izbrisan oglas tako izgine takoj.
     * Ciscenje je varno samo tu, ker je poizvedba nefiltrirana in je streznik
     * merodajen za cel nabor.
     */
    suspend fun syncAllPage(createdBefore: String?, limit: Int, oznaka: Long): StranOglasov {
        // Brez seje RLS ne vrne nicesar. Tega ne smemo razumeti kot "streznik nima
        // oglasov", sicer bi odjava ali potekel zeton izpraznila predpomnilnik.
        if (authRepository.currentUserId() == null) return StranOglasov(0, null, jeKonec = true)

        val fresh = getListingsPage(createdBefore = createdBefore, limit = limit)
        listingDao.upsertAll(fresh.map { it.toEntity(oznaka) })
        val jeKonec = fresh.size < limit
        if (jeKonec) listingDao.deleteStale(cutoff = oznaka)
        return StranOglasov(fresh.size, fresh.lastOrNull()?.createdAt, jeKonec)
    }

    /**
     * F10: ena stran zadetkov iskanja.
     *
     * Poizvedba je filtrirana, zato zapisov, ki filtru ne ustrezajo, ne smemo brisati --
     * pripadajo drugim iskanjem. Odstranijo se sele po zastaranju.
     */
    suspend fun syncSearchPage(
        location: String?,
        maxPrice: Double?,
        createdBefore: String?,
        limit: Int
    ): StranOglasov {
        val now = System.currentTimeMillis()
        val fresh = getListingsPage(location, maxPrice, createdBefore, limit)
        listingDao.syncListings(fresh.map { it.toEntity(now) }, cutoff = now - STALE_AFTER_MS)
        return StranOglasov(fresh.size, fresh.lastOrNull()?.createdAt, jeKonec = fresh.size < limit)
    }

    /**
     * Polna sinhronizacija brez filtrov in brez meje -- uporablja jo SyncWorker v ozadju.
     *
     * Namenoma ni strancena: `replaceAll` sme brisati samo, ce je bil prenesen cel nabor,
     * in prav ta obhod je jamstvo, da izbrisani oglasi izginejo tudi, ce uporabnik
     * seznama nikoli ne prelista do konca.
     */
    suspend fun syncAllFromRemote() {
        // Glej opombo v syncAllPage(): brez seje bi replaceAll() pobrisal predpomnilnik.
        if (authRepository.currentUserId() == null) return

        val now = System.currentTimeMillis()
        val fresh = getListings()
        listingDao.replaceAll(fresh.map { it.toEntity(now) })
    }

    /**
     * F07/F10/F11: ena stran oglasov s strani streznika.
     *
     * Strancenje je kazalcno (keyset), ne z OFFSET: `created_at < zadnji_videni` bere
     * natanko `limit` vrstic iz indeksa, medtem ko mora OFFSET vse prejsnje vrstice
     * prebrati in zavreci. Zato je cena strani enaka ne glede na globino.
     */
    suspend fun getListingsPage(
        location: String? = null,
        maxPrice: Double? = null,
        createdBefore: String? = null,
        limit: Int = VELIKOST_STRANI
    ): List<Listing> =
        supabase.from("listings").select(LISTING_COLUMNS) {
            filter {
                eq("is_filled", false)
                if (!location.isNullOrBlank()) ilike("location", "%${location.trim()}%")
                if (maxPrice != null) lte("price_per_month", maxPrice)
                if (createdBefore != null) lt("created_at", createdBefore)
            }
            order("created_at", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList<ListingDto>().map { it.toDomain() }

    /** F07/F10/F11: seznam oglasov (samo nezasedeni), z izbirnim filtrom lokacije in cene. */
    suspend fun getListings(location: String? = null, maxPrice: Double? = null): List<Listing> =
        supabase.from("listings").select(LISTING_COLUMNS) {
            filter {
                eq("is_filled", false)
                if (!location.isNullOrBlank()) ilike("location", "%${location.trim()}%")
                if (maxPrice != null) lte("price_per_month", maxPrice)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<ListingDto>().map { it.toDomain() }

    /** F08: podrobnosti posameznega oglasa. */
    suspend fun getListing(id: String): Listing? =
        supabase.from("listings").select(LISTING_COLUMNS) { filter { eq("id", id) } }
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
        /** Stevilo oglasov na stran. */
        const val VELIKOST_STRANI = 20

        /** Zapisi, ki jih sinhronizacija ni osvezila 7 dni, se odstranijo iz predpomnilnika. */
        private const val STALE_AFTER_MS = 7L * 24 * 60 * 60 * 1000

        /**
         * Beremo samo stolpce, ki jih ListingDto res uporabi. Privzeti select() poslje
         * SELECT *, kar po nepotrebnem prenasa se preostale stolpce tabele.
         */
        private val LISTING_COLUMNS = Columns.list(
            "id", "owner_id", "location", "price_per_month", "description", "is_filled", "created_at"
        )
    }
}
