package com.roomieslo.app.data.remote.dto

import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.domain.model.Profile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Prenosni objekt za tabelo `public.profiles` (PostgREST).
 * Imena polj ustrezajo stolpcem v bazi (snake_case) prek @SerialName.
 *
 * Predstavitvena polja (age, faculty, bio, avatar_url) so iz migracije 0002 in imajo
 * privzete vrednosti, ker jih profili, ustvarjeni pred migracijo, nimajo izpolnjenih.
 */
@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("academic_status_verified") val academicStatusVerified: Boolean = false,
    @SerialName("is_available") val isAvailable: Boolean = true,
    val age: Int? = null,
    val faculty: String = "",
    val bio: String = "",
    @SerialName("avatar_url") val avatarUrl: String = ""
) {
    fun toDomain(lifestyleAnswers: List<LifestyleAnswer> = emptyList()) = Profile(
        userId = id,
        displayName = displayName,
        academicStatusVerified = academicStatusVerified,
        isAvailable = isAvailable,
        lifestyleAnswers = lifestyleAnswers,
        age = age,
        faculty = faculty,
        bio = bio,
        avatarUrl = avatarUrl
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
    val age: Int? = null,
    val faculty: String = "",
    val bio: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("questionnaire_answers") val answers: List<QuestionnaireAnswerDto> = emptyList()
) {
    fun toDomain() = Profile(
        userId = id,
        displayName = displayName,
        academicStatusVerified = academicStatusVerified,
        isAvailable = isAvailable,
        lifestyleAnswers = answers.map { it.toDomain() },
        age = age,
        faculty = faculty,
        bio = bio,
        avatarUrl = avatarUrl
    )
}
