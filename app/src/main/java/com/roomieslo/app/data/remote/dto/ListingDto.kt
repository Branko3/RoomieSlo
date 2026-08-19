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
    @SerialName("created_at") val createdAt: String = "",
    // Polja iz migracije 0002. Privzete vrednosti so nujne: vrstice, ustvarjene pred
    // migracijo, teh kljucev v odgovoru nimajo, PostgREST pa jih vrne kot null.
    val title: String = "",
    @SerialName("room_type") val roomType: String = "",
    val district: String = "",
    @SerialName("available_from") val availableFrom: String? = null,
    @SerialName("size_sqm") val sizeSqm: Int? = null,
    val deposit: Double? = null,
    @SerialName("bills_included") val billsIncluded: Boolean = false,
    val furnished: Boolean = false,
    @SerialName("flatmates_count") val flatmatesCount: Int = 0,
    @SerialName("photo_url") val photoUrl: String = ""
) {
    fun toDomain() = Listing(
        id = id,
        ownerId = ownerId,
        location = location,
        pricePerMonth = pricePerMonth,
        description = description,
        isFilled = isFilled,
        createdAt = createdAt,
        title = title,
        roomType = roomType,
        district = district,
        availableFrom = availableFrom,
        sizeSqm = sizeSqm,
        deposit = deposit,
        billsIncluded = billsIncluded,
        furnished = furnished,
        flatmatesCount = flatmatesCount,
        photoUrl = photoUrl
    )
}

/** Objekt za vstavljanje novega oglasa (brez id/version/created_at — te doloci baza). */
@Serializable
data class NewListingDto(
    @SerialName("owner_id") val ownerId: String,
    val location: String,
    @SerialName("price_per_month") val pricePerMonth: Double,
    val description: String,
    val title: String = "",
    @SerialName("room_type") val roomType: String = "",
    val district: String = "",
    @SerialName("available_from") val availableFrom: String? = null,
    @SerialName("size_sqm") val sizeSqm: Int? = null,
    val deposit: Double? = null,
    @SerialName("bills_included") val billsIncluded: Boolean = false,
    val furnished: Boolean = false,
    @SerialName("flatmates_count") val flatmatesCount: Int = 0
)
