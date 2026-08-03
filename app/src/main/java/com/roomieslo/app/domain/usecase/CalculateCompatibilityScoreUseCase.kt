package com.roomieslo.app.domain.usecase

import com.roomieslo.app.domain.model.Profile
import javax.inject.Inject

/**
 * F11, F12, F13: izracun zdruzljivosti med dvema profiloma.
 * Utezena vsota po trditvah vprasalnika o zivljenjskem slogu -- brez umetne
 * inteligence oziroma strojnega ucenja.
 *
 * S(u,v) = sum_i( w_i * sim_i(u,v) ),  sum_i(w_i) = 1
 *
 * Utez trditve je manjsa od obeh utezi: odgovor "Ne zelim odgovoriti" je shranjen z
 * utezjo 0, zato taka trditev odpade ne glede na to, kateri od profilov je ni podal.
 * Brez tega bi nevtralna vrednost neizrecenega odgovora navidezno povecala ujemanje.
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
