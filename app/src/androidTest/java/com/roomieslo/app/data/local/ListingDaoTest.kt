package com.roomieslo.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.roomieslo.app.data.local.dao.ListingDao
import com.roomieslo.app.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testi predpomnilnika oglasov (F10 + lokalno predpomnjenje).
 *
 * Baza tece v pomnilniku, zato je vsak test popolnoma izoliran --
 * po vsakem testu se podatki zavrzejo.
 */
@RunWith(AndroidJUnit4::class)
class ListingDaoTest {

    private lateinit var db: RoomieSloDatabase
    private lateinit var dao: ListingDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomieSloDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.listingDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** Pomozna metoda, da testi navajajo samo tisto, kar je zanje pomembno. */
    private fun listing(
        id: String,
        location: String = "Ljubljana",
        price: Double = 300.0,
        isFilled: Boolean = false,
        syncedAt: Long = 1_000L,
        createdAt: String = "2026-01-01T00:00:00Z"
    ) = ListingEntity(
        id = id,
        ownerId = "owner-$id",
        location = location,
        pricePerMonth = price,
        description = "opis",
        isFilled = isFilled,
        createdAt = createdAt,
        lastSyncedAt = syncedAt
    )

    @Test
    fun delniNizVrneUjemajociSeOglas() = runTest {
        dao.upsertAll(listOf(listing(id = "1", location = "Ljubljana")))

        val rows = dao.observeListings(location = "ljub", maxPrice = 500.0, limit = 100).first()

        // Hkrati preveri, da je LIKE v SQLite neobcutljiv na velike crke.
        assertEquals(listOf("1"), rows.map { it.id })
    }

    @Test
    fun praznaLokacijaVrneVseOglase() = runTest {
        dao.upsertAll(
            listOf(
                listing(id = "1", location = "Ljubljana"),
                listing(id = "2", location = "Maribor")
            )
        )

        val rows = dao.observeListings(location = "", maxPrice = 500.0, limit = 100).first()

        assertEquals(2, rows.size)
    }

    @Test
    fun lokacijaKiSeNeUjemaNeVrneNicesar() = runTest {
        dao.upsertAll(listOf(listing(id = "1", location = "Ljubljana")))

        val rows = dao.observeListings(location = "Koper", maxPrice = 500.0, limit = 100).first()

        assertEquals(emptyList<String>(), rows.map { it.id })
    }

    @Test
    fun zgornjaMejaCeneJeVkljucujoca() = runTest {
        dao.upsertAll(
            listOf(
                listing(id = "1", price = 400.0),
                listing(id = "2", price = 400.01)
            )
        )

        val rows = dao.observeListings(location = "", maxPrice = 400.0, limit = 100).first()

        assertEquals(listOf("1"), rows.map { it.id })
    }

    @Test
    fun zasedeniOglasiNisoVrnjeni() = runTest {
        dao.upsertAll(
            listOf(
                listing(id = "1", isFilled = false),
                listing(id = "2", isFilled = true)
            )
        )

        val rows = dao.observeListings(location = "", maxPrice = 500.0, limit = 100).first()

        assertEquals(listOf("1"), rows.map { it.id })
    }

    @Test
    fun ponovniUpsertPrepiseObstojeciZapis() = runTest {
        dao.upsertAll(listOf(listing(id = "1", price = 300.0)))
        dao.upsertAll(listOf(listing(id = "1", price = 250.0)))

        val rows = dao.observeListings(location = "", maxPrice = 500.0, limit = 100).first()

        // OnConflictStrategy.REPLACE: en sam zapis z novo ceno, ne dva.
        assertEquals(1, rows.size)
        assertEquals(250.0, rows.first().pricePerMonth, 0.001)
    }

    @Test
    fun deleteStaleOdstraniStareInObdrziSveze() = runTest {
        dao.upsertAll(
            listOf(
                listing(id = "star", syncedAt = 1_000L),
                listing(id = "svez", syncedAt = 5_000L)
            )
        )

        dao.deleteStale(cutoff = 3_000L)

        val rows = dao.observeListings(location = "", maxPrice = 500.0, limit = 100).first()
        assertEquals(listOf("svez"), rows.map { it.id })
    }

    @Test
    fun syncListingsVpiseNoveInOdstraniNeosvezene() = runTest {
        // Stanje predpomnilnika pred sinhronizacijo: dva oglasa iz prejsnjega osvezevanja.
        dao.upsertAll(
            listOf(
                listing(id = "obstaja", syncedAt = 1_000L),
                listing(id = "izbrisan", syncedAt = 1_000L)
            )
        )

        // Streznik vrne samo "obstaja" (z novim casom) in en nov oglas.
        val now = 10_000L
        dao.syncListings(
            fresh = listOf(
                listing(id = "obstaja", syncedAt = now),
                listing(id = "nov", syncedAt = now)
            ),
            cutoff = now - 5_000L
        )

        val rows = dao.observeListings(location = "", maxPrice = 500.0, limit = 100).first()

        // "izbrisan" ni bil osvezen, zato izpade; ostala dva ostaneta.
        assertEquals(listOf("nov", "obstaja"), rows.map { it.id }.sorted())
    }

    /* ---------- Strancenje in razvrscanje ---------- */

    @Test
    fun oglasiSoRazvrsceniOdNajnovejsegaProtiStarejsemu() = runTest {
        dao.upsertAll(
            listOf(
                listing(id = "star", createdAt = "2026-01-01T00:00:00Z"),
                listing(id = "nov", createdAt = "2026-03-01T00:00:00Z"),
                listing(id = "srednji", createdAt = "2026-02-01T00:00:00Z")
            )
        )

        val rows = dao.observeAll(limit = 100).first()

        assertEquals(listOf("nov", "srednji", "star"), rows.map { it.id })
    }

    @Test
    fun mejaVrneSamoPrvoStran() = runTest {
        dao.upsertAll(
            listOf(
                listing(id = "1", createdAt = "2026-03-01T00:00:00Z"),
                listing(id = "2", createdAt = "2026-02-01T00:00:00Z"),
                listing(id = "3", createdAt = "2026-01-01T00:00:00Z")
            )
        )

        // Zaslon prikaze prvo stran; preostali oglasi ostanejo v predpomnilniku.
        assertEquals(listOf("1", "2"), dao.observeAll(limit = 2).first().map { it.id })

        // Ko uporabnik pride do dna, se meja poveca in Room odda sirsi nabor.
        assertEquals(listOf("1", "2", "3"), dao.observeAll(limit = 10).first().map { it.id })
    }

    @Test
    fun mejaVeljaTudiZaIskanje() = runTest {
        dao.upsertAll(
            listOf(
                listing(id = "1", location = "Ljubljana", createdAt = "2026-03-01T00:00:00Z"),
                listing(id = "2", location = "Ljubljana", createdAt = "2026-02-01T00:00:00Z"),
                listing(id = "3", location = "Maribor", createdAt = "2026-01-01T00:00:00Z")
            )
        )

        val rows = dao.observeListings(location = "ljubljana", maxPrice = 500.0, limit = 1).first()

        assertEquals(listOf("1"), rows.map { it.id })
    }

    @Test
    fun oznakaObhodaPobriseSamoZapiseStarejsihObhodov() = runTest {
        // Prejsnji obhod je oznacil oba oglasa.
        val prejsnjiObhod = 1_000L
        dao.upsertAll(
            listOf(
                listing(id = "ostane", syncedAt = prejsnjiObhod),
                listing(id = "izbrisan-na-strezniku", syncedAt = prejsnjiObhod)
            )
        )

        // Nov obhod po straneh: streznik vrne samo "ostane" in en nov oglas,
        // obe strani pa nosita isto oznako.
        val obhod = 2_000L
        dao.upsertAll(listOf(listing(id = "ostane", syncedAt = obhod)))
        dao.upsertAll(listOf(listing(id = "nov", syncedAt = obhod)))

        // Konec nabora -> pocistimo vse, cesar ta obhod ni osvezil.
        dao.deleteStale(cutoff = obhod)

        val rows = dao.observeAll(limit = 100).first()
        assertEquals(listOf("nov", "ostane"), rows.map { it.id }.sorted())
    }
}
