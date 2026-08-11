package com.roomieslo.app.domain.usecase

import com.roomieslo.app.domain.model.Profile
import javax.inject.Inject

/**
 * F11, F12, F13: izracun zdruzljivosti med dvema profiloma.
 * Utezena mera podobnosti odgovorov na vprasalnik o zivljenjskem slogu.
 *
 * S(u,v) = sum_i( w_i * sim_i(u,v) ) / sum_i( w_i )
 *
 * Delimo z vsoto utezi tistih trditev, na katere sta odgovorila oba, zato je rezultat
 * na intervalu 0..1 ne glede na stevilo odgovorjenih trditev.
 *
 * Utez trditve je manjsa od obeh utezi: odgovor "Ne želim odgovoriti" je shranjen z
 * utezjo 0, zato taka trditev odpade ne glede na to, kateri od profilov je ni podal.
 * Brez tega bi nevtralna vrednost neizrecenega odgovora navidezno povecala ujemanje.
 *
 * Utezi posameznih trditev izhajajo iz ankete med ciljno skupino -- glej Vprasalnik.
 */
class CalculateCompatibilityScoreUseCase @Inject constructor() {

    operator fun invoke(a: Profile, b: Profile): Float {
        val byQuestion = b.lifestyleAnswers.associateBy { it.questionId }
        var weightedSum = 0f
        var weightTotal = 0f

        for (answerA in a.lifestyleAnswers) {
            val answerB = byQuestion[answerA.questionId] ?: continue
            val weight = minOf(answerA.weight, answerB.weight)
            if (weight <= 0f) continue
            val similarity = 1f - kotlin.math.abs(answerA.value - answerB.value)
            weightedSum += weight * similarity
            weightTotal += weight
        }

        return if (weightTotal > 0f) weightedSum / weightTotal else 0f
    }
}
