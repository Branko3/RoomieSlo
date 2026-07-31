package com.roomieslo.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Branje id-jev priljubljenih oglasov trenutnega uporabnika. */
@Serializable
data class FavoriteDto(
    @SerialName("listing_id") val listingId: String
)

/** Vstavljanje priljubljenega (profile_id = trenutni uporabnik). */
@Serializable
data class NewFavoriteDto(
    @SerialName("profile_id") val profileId: String,
    @SerialName("listing_id") val listingId: String
)
