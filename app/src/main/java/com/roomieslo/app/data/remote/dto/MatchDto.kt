package com.roomieslo.app.data.remote.dto

import com.roomieslo.app.domain.model.Match
import com.roomieslo.app.domain.model.MatchStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Prenosni objekt za branje tabele `public.matches` (PostgREST). */
@Serializable
data class MatchDto(
    val id: String,
    @SerialName("user_id_a") val userIdA: String,
    @SerialName("user_id_b") val userIdB: String,
    val status: String = "pending",
    val version: Int = 1
) {
    fun toDomain() = Match(
        id = id,
        userIdA = userIdA,
        userIdB = userIdB,
        status = MatchStatus.valueOf(status.uppercase()),
        version = version
    )
}

/** Objekt za vstavljanje nove zahteve za ujemanje (user_id_a = pošiljatelj). */
@Serializable
data class NewMatchDto(
    @SerialName("user_id_a") val userIdA: String,
    @SerialName("user_id_b") val userIdB: String
)
