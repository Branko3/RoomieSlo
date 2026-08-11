package com.roomieslo.app.domain.usecase

import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.domain.model.Profile
import com.roomieslo.app.domain.model.Vprasalnik
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Preverja izracun zdruzljivosti (F11, F12, F13) na sinteticnih profilih z znanimi
 * pricakovanimi vrednostmi.
 *
 * Utezi se berejo iz [Vprasalnik], ne iz konstant v testu: ce se utezi po novi anketi
 * spremenijo, testi preverjajo isto lastnost formule in ne odpovejo brez razloga.
 */
class CalculateCompatibilityScoreUseCaseTest {

    private val izracunaj = CalculateCompatibilityScoreUseCase()

    private fun utez(idTrditve: String): Float =
        Vprasalnik.trditve.first { it.id == idTrditve }.utez

    /** Profil z odgovori, podanimi kot pari id trditve in normalizirane vrednosti. */
    private fun profil(vararg odgovori: Pair<String, Float>) = Profile(
        userId = "u",
        displayName = "Test",
        academicStatusVerified = true,
        isAvailable = true,
        lifestyleAnswers = odgovori.map { (id, vrednost) ->
            LifestyleAnswer(questionId = id, value = vrednost, weight = utez(id))
        }
    )

    @Test
    fun `popolnoma enaki odgovori dajo najvisjo oceno`() {
        val a = profil("q_cleanliness" to 1f, "q_noise" to 0.25f, "q_wake" to 0.5f)
        val b = profil("q_cleanliness" to 1f, "q_noise" to 0.25f, "q_wake" to 0.5f)
        assertEquals(1f, izracunaj(a, b), DELTA)
    }

    @Test
    fun `popolnoma nasprotni odgovori dajo najnizjo oceno`() {
        val a = profil("q_cleanliness" to 1f, "q_smoking" to 0f)
        val b = profil("q_cleanliness" to 0f, "q_smoking" to 1f)
        assertEquals(0f, izracunaj(a, b), DELTA)
    }

    @Test
    fun `brez skupne odgovorjene trditve je ocena nic`() {
        val a = profil("q_cleanliness" to 1f)
        val b = profil("q_smoking" to 1f)
        assertEquals(0f, izracunaj(a, b), DELTA)
    }

    @Test
    fun `trditev, na katero je odgovoril le en uporabnik, se ne steje`() {
        // Enako ujemanje kot brez dodatne trditve: q_smoking ima le uporabnik a.
        val a = profil("q_cleanliness" to 1f, "q_smoking" to 0f)
        val b = profil("q_cleanliness" to 1f)
        assertEquals(1f, izracunaj(a, b), DELTA)
    }

    @Test
    fun `neizrecen odgovor izloci trditev ne glede na to, kdo ga ni podal`() {
        val neizrecen = LifestyleAnswer("q_cleanliness", 0.5f, 0f)
        val izrecen = LifestyleAnswer("q_cleanliness", 1f, utez("q_cleanliness"))
        val ujemanje = LifestyleAnswer("q_wake", 0.5f, utez("q_wake"))

        val a = Profile("u", "A", true, true, listOf(izrecen, ujemanje))
        val b = Profile("v", "B", true, true, listOf(neizrecen, ujemanje))

        // Ostane samo q_wake, kjer se odgovora ujemata -- ocena je 1, ceprav se pri
        // q_cleanliness vrednosti razlikujeta.
        assertEquals(1f, izracunaj(a, b), DELTA)
        // Formula je simetricna: vrstni red profilov na rezultat ne vpliva.
        assertEquals(izracunaj(a, b), izracunaj(b, a), DELTA)
    }

    @Test
    fun `neujemanje pri trditvi z vecjo utezjo bolj znizza oceno`() {
        // Najvecja in najmanjsa utez iz vprasalnika.
        val visoka = "q_cleanliness"
        val nizka = "q_wake"

        // Ujemanje pri trditvi z visoko utezjo, neujemanje pri nizki.
        val a1 = profil(visoka to 1f, nizka to 1f)
        val b1 = profil(visoka to 1f, nizka to 0f)

        // Obratno: neujemanje pri trditvi z visoko utezjo.
        val a2 = profil(visoka to 1f, nizka to 1f)
        val b2 = profil(visoka to 0f, nizka to 1f)

        val ocenaVisokaUjema = izracunaj(a1, b1)
        val ocenaVisokaNeUjema = izracunaj(a2, b2)

        assertTrue(
            "Neujemanje pri pomembnejsi trditvi mora bolj znizati oceno " +
                "($ocenaVisokaUjema vs $ocenaVisokaNeUjema)",
            ocenaVisokaUjema > ocenaVisokaNeUjema
        )

        // Vsota obeh ocen je 1: skupaj pokrijeta celotno utez obeh trditev.
        assertEquals(1f, ocenaVisokaUjema + ocenaVisokaNeUjema, DELTA)
    }

    @Test
    fun `ocena je normalizirana ne glede na stevilo odgovorjenih trditev`() {
        // Delno ujemanje (razlika 0,25) pri eni sami trditvi ...
        val a1 = profil("q_cleanliness" to 1f)
        val b1 = profil("q_cleanliness" to 0.75f)

        // ... da enako oceno kot isto delno ujemanje pri vseh enajstih trditvah.
        val vse = Vprasalnik.trditve.map { it.id }
        val a2 = profil(*vse.map { it to 1f }.toTypedArray())
        val b2 = profil(*vse.map { it to 0.75f }.toTypedArray())

        assertEquals(0.75f, izracunaj(a1, b1), DELTA)
        assertEquals(izracunaj(a1, b1), izracunaj(a2, b2), DELTA)
    }

    private companion object {
        const val DELTA = 1e-4f
    }
}
