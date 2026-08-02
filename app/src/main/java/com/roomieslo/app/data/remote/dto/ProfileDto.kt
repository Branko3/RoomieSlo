package com.roomieslo.app.data.remote.dto

import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.domain.model.Profile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Prenosni objekt za tabelo `public.profiles` (PostgREST).
 * Imena polj ustrezajo stolpcem v bazi (snake_case) prek @SerialName.
 */
@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("academic_status_verified") val academicStatusVerified: Boolean = false,
    @SerialName("is_available") val isAvailable: Boolean = true
) {
    fun toDomain(lifestyleAnswers: List<LifestyleAnswer> = emptyList()) = Profile(
        userId = id,
        displayName = displayName,
        academicStatusVerified = academicStatusVerified,
        isAvailable = isAvailable,
        lifestyleAnswers = lifestyleAnswers
    )
}

/**
 * Profil z vgnezdenimi odgovori vprasalnika.
 *
 * PostgREST zdruzi tabeli prek tujega kljuca questionnaire_answers.profile_id ->
 * profiles.id, zato priporocila (F12/F13) potrebujejo eno zahtevo namesto dveh.
 */
@Serializable
data class ProfileWithAnswersDto(
    val id: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("academic_status_verified") val academicStatusVerified: Boolean = false,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("questionnaire_answers") val answers: List<QuestionnaireAnswerDto> = emptyList()
) {
    fun toDomain() = Profile(
        userId = id,
        displayName = displayName,
        academicStatusVerified = academicStatusVerified,
        isAvailable = isAvailable,
        lifestyleAnswers = answers.map { it.toDomain() }
    )
}
