package com.roomieslo.app.data.remote.dto

import com.roomieslo.app.domain.model.Listing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Prenosni objekt za branje tabele `public.listings` (PostgREST). */
@Serializable
data class ListingDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val location: String,
    @SerialName("price_per_month") val pricePerMonth: Double,
    val description: String = "",
    @SerialName("is_filled") val isFilled: Boolean = false,
    @SerialName("created_at") val createdAt: String = ""
) {
    fun toDomain() = Listing(
        id = id,
        ownerId = ownerId,
        location = location,
        pricePerMonth = pricePerMonth,
        description = description,
        isFilled = isFilled,
        createdAt = createdAt
    )
}

/** Objekt za vstavljanje novega oglasa (brez id/version/created_at — te doloci baza). */
@Serializable
data class NewListingDto(
    @SerialName("owner_id") val ownerId: String,
    val location: String,
    @SerialName("price_per_month") val pricePerMonth: Double,
    val description: String
)
