package com.roomieslo.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Branje prijave uporabnika (za administratorsko ploščo). */
@Serializable
data class ReportDto(
    val id: String,
    @SerialName("reported_id") val reportedId: String,
    val reason: String,
    val description: String = "",
    val status: String = "open"
)

/** Vstavljanje nove prijave (reporter_id = trenutni uporabnik). */
@Serializable
data class NewReportDto(
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("reported_id") val reportedId: String,
    val reason: String,
    val description: String
)

/** Vrstica iz tabele `admins` (za preverjanje administratorskih pravic). */
@Serializable
data class AdminDto(
    @SerialName("user_id") val userId: String
)
