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
