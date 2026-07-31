package com.roomieslo.app.domain.usecase

import com.roomieslo.app.domain.model.Profile
import javax.inject.Inject

/**
 * F11, F12, F13: izracun zdruzljivosti med dvema profiloma.
 * Utezena vsota po vprasanjih vprasalnika o zivljenjskem slogu -- brez umetne
 * inteligence oziroma strojnega ucenja (glej diplomsko delo, razdelek "Formula zdruzljivosti").
 *
 * S(u,v) = sum_i( w_i * sim_i(u,v) ),  sum_i(w_i) = 1
 */
class CalculateCompatibilityScoreUseCase @Inject constructor() {

    operator fun invoke(a: Profile, b: Profile): Float {
        val byQuestion = b.lifestyleAnswers.associateBy { it.questionId }
        var weightedSum = 0f
        var weightTotal = 0f

        for (answerA in a.lifestyleAnswers) {
            val answerB = byQuestion[answerA.questionId] ?: continue
            val similarity = 1f - kotlin.math.abs(answerA.value - answerB.value)
            weightedSum += answerA.weight * similarity
            weightTotal += answerA.weight
        }

        return if (weightTotal > 0f) weightedSum / weightTotal else 0f
    }
}
