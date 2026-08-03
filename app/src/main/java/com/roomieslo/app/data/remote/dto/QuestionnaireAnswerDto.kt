package com.roomieslo.app.data.remote.dto

import com.roomieslo.app.domain.model.LifestyleAnswer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Prenosni objekt za tabelo `public.questionnaire_answers` (PostgREST).
 * Ena vrstica = en odgovor uporabnika na eno vprasanje vprasalnika (F04).
 *
 * `weight` namenoma nima privzete vrednosti. kotlinx.serialization polj, ki so enaka
 * privzeti vrednosti, ne zapise v JSON; PostgREST bi tak manjkajoci stolpec vstavil kot
 * NULL, kar krsi omejitev NOT NULL. Utez je vedno znana, zato jo vedno tudi posljemo.
 */
@Serializable
data class QuestionnaireAnswerDto(
    @SerialName("profile_id") val profileId: String,
    @SerialName("question_id") val questionId: String,
    val value: Float,
    val weight: Float
) {
    fun toDomain() = LifestyleAnswer(questionId = questionId, value = value, weight = weight)
}
