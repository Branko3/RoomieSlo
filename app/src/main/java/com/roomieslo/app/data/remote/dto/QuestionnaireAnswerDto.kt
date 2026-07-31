package com.roomieslo.app.data.remote.dto

import com.roomieslo.app.domain.model.LifestyleAnswer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Prenosni objekt za tabelo `public.questionnaire_answers` (PostgREST).
 * Ena vrstica = en odgovor uporabnika na eno vprasanje vprasalnika (F04).
 */
@Serializable
data class QuestionnaireAnswerDto(
    @SerialName("profile_id") val profileId: String,
    @SerialName("question_id") val questionId: String,
    val value: Float,
    val weight: Float = 1f
) {
    fun toDomain() = LifestyleAnswer(questionId = questionId, value = value, weight = weight)
}
