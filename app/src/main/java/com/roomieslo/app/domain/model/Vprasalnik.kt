package com.roomieslo.app.domain.model

import kotlin.math.abs

/**
 * Stopnja strinjanja s trditvijo vprasalnika (F04).
 *
 * `vrednost` je normalizirana na 0f..1f, ker izracun zdruzljivosti primerja odgovore
 * z razliko `1 - |a - b|` (glej CalculateCompatibilityScoreUseCase).
 *
 * `mnozitelj` ni utez trditve -- ta je zapisana pri [Trditev.utez] -- ampak pove le, ali je
 * uporabnik stopnjo sploh izrekel. Utez, ki se zapise v bazo, je zmnozek obeh, zato pri
 * [NE_ZELIM_ODGOVORITI] znese 0: vrstica se v bazo vseeno zapise, tako da ima vsak uporabnik
 * odgovor na vsako trditev, na rezultat zdruzljivosti pa neizrecen odgovor ne vpliva.
 */
enum class LikertOdgovor(val besedilo: String, val vrednost: Float, val mnozitelj: Float) {
    SPLOH_SE_NE_STRINJAM("Sploh se ne strinjam", 0f, 1f),
    SE_NE_STRINJAM("Se ne strinjam", 0.25f, 1f),
    NITI_NITI("Niti da niti ne", 0.5f, 1f),
    SE_STRINJAM("Se strinjam", 0.75f, 1f),
    POPOLNOMA_SE_STRINJAM("Popolnoma se strinjam", 1f, 1f),
    NE_ZELIM_ODGOVORITI("Ne želim odgovoriti", 0.5f, 0f);

    /** Ali odgovor steje pri izracunu zdruzljivosti. */
    val jeIzrecen: Boolean get() = mnozitelj > 0f

    companion object {
        /**
         * Poisce stopnjo, ki ustreza ze shranjenemu odgovoru.
         *
         * Utez 0 pomeni, da uporabnik odgovora ni zelel podati. Sicer vzamemo najblizjo
         * stopnjo: shranjene vrednosti niso nujno tocno na lestvici, ker je prejsnja
         * razlicica vprasalnika uporabljala zvezni drsnik.
         */
        fun izVrednosti(vrednost: Float, utez: Float): LikertOdgovor =
            if (utez <= 0f) NE_ZELIM_ODGOVORITI
            else entries.filter { it.jeIzrecen }.minBy { abs(it.vrednost - vrednost) }
    }
}

/**
 * Ena trditev vprasalnika o zivljenjskem slogu (F04).
 *
 * `utez` pove, koliko trditev steje pri izracunu zdruzljivosti. Ni ocena avtorja, ampak
 * izhaja iz ankete med ciljno skupino -- glej [Vprasalnik].
 */
data class Trditev(val id: String, val besedilo: String, val utez: Float)

/**
 * Nabor trditev vprasalnika. Trditve so zapisane kot izjave, ne kot vprasanja,
 * ker uporabnik izraza stopnjo strinjanja z njimi.
 *
 * Utezi so izracunane iz ankete med studenti v Sloveniji (n = 20, avgust 2026). Anketiranci so
 * za vsako temo na lestvici 1..5 ocenili, kako pomembno jim je, da se s sostanovalcem ujemata;
 * utez je povprecje teme, deljeno z vsoto povprecij vseh ohranjenih tem. Tri teme s povprecjem
 * pod 2,5 (skupno kuhanje, cas preziveti skupaj, jezik doma) v vprasalnik niso vkljucene.
 *
 * Vsota utezi je 0,9999 in ne tocno 1 zaradi zaokrozevanja. To ni napaka in ne potrebuje
 * popravka: izracun zdruzljivosti deli z vsoto utezi tistih trditev, na katere sta odgovorila
 * oba uporabnika, zato skupna vsota na rezultat ne vpliva.
 *
 * Trditve so nastete od najvecje utezi navzdol. Uporabnik lahko vprasalnik kadar koli preskoci,
 * zato so na vrhu tiste, ki na rezultat najbolj vplivajo.
 *
 * Id-jev ni dovoljeno spreminjati: so kljuc vrstic v tabeli `questionnaire_answers`
 * in povezujejo ze shranjene odgovore s to lestvico.
 */
object Vprasalnik {
    val trditve = listOf(
        Trditev("q_cleanliness", "Skupni prostori morajo biti vedno pospravljeni.", 0.1099f),
        Trditev("q_costs", "Svoj del stroškov poravnam do dogovorjenega roka.", 0.1056f),
        Trditev("q_smoking", "Kajenje v stanovanju me ne moti.", 0.1008f),
        Trditev("q_chores", "Gospodinjska opravila naj bodo razdeljena po dogovorjenem razporedu.", 0.0992f),
        Trditev("q_parties", "Občasne zabave v stanovanju me ne motijo.", 0.0952f),
        Trditev("q_pets", "V stanovanju bi rad(a) imel(a) hišnega ljubljenčka.", 0.0950f),
        Trditev("q_noise", "Zvečer imam rad(a) mir in tišino.", 0.0904f),
        Trditev("q_study", "Doma se pogosto učim in takrat potrebujem mir.", 0.0856f),
        Trditev("q_guests", "Vesel(a) sem, če so v stanovanju pogosto obiski.", 0.0820f),
        Trditev("q_overnight", "Ne moti me, če gostje ali partner(ka) prespijo v stanovanju.", 0.0687f),
        Trditev("q_wake", "Zjutraj vstajam zgodaj in zvečer hodim zgodaj spat.", 0.0675f)
    )
}
