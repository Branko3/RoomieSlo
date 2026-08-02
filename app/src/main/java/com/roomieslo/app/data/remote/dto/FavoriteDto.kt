package com.roomieslo.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Branje id-jev priljubljenih oglasov trenutnega uporabnika. */
@Serializable
data class FavoriteDto(
    @SerialName("listing_id") val listingId: String
)

/**
 * Priljubljeni z vgnezdenim oglasom -- PostgREST vrne oboje v enem odgovoru.
 * `listings` je lahko null, ce je bil oglas medtem izbrisan.
 */
@Serializable
data class FavoriteWithListingDto(
    @SerialName("listing_id") val listingId: String,
    @SerialName("listings") val listing: ListingDto? = null
)

/** Vstavljanje priljubljenega (profile_id = trenutni uporabnik). */
@Serializable
data class NewFavoriteDto(
    @SerialName("profile_id") val profileId: String,
    @SerialName("listing_id") val listingId: String
)
