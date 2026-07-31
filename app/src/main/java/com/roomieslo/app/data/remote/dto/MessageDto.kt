package com.roomieslo.app.data.remote.dto

import com.roomieslo.app.domain.model.DeliveryStatus
import com.roomieslo.app.domain.model.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Prenosni objekt za branje tabele `public.messages` (PostgREST). */
@Serializable
data class MessageDto(
    val id: String,
    @SerialName("match_id") val matchId: String,
    @SerialName("sender_id") val senderId: String,
    val body: String,
    @SerialName("delivery_status") val deliveryStatus: String = "sent"
) {
    fun toDomain() = Message(
        id = id,
        matchId = matchId,
        senderId = senderId,
        body = body,
        deliveryStatus = DeliveryStatus.valueOf(deliveryStatus.uppercase())
    )
}

/** Objekt za vstavljanje novega sporocila (sender_id = trenutni uporabnik). */
@Serializable
data class NewMessageDto(
    @SerialName("match_id") val matchId: String,
    @SerialName("sender_id") val senderId: String,
    val body: String
)
