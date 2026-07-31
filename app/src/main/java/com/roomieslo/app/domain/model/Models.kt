package com.roomieslo.app.domain.model

/** F03 */
data class Profile(
    val userId: String,
    val displayName: String,
    val academicStatusVerified: Boolean, // F02
    val isAvailable: Boolean,            // F05
    val lifestyleAnswers: List<LifestyleAnswer> // F04
)

/** Odgovor na eno vprasanje vprasalnika o zivljenjskem slogu (F04). */
data class LifestyleAnswer(
    val questionId: String,
    val value: Float,  // normalizirana vrednost odgovora, npr. 0f..1f
    val weight: Float  // utez vprasanja pri izracunu zdruzljivosti (F12)
)

/** F06 */
data class Listing(
    val id: String,
    val ownerId: String,
    val location: String,
    val pricePerMonth: Double,
    val description: String,
    val isFilled: Boolean // F09
)

/** F15 */
enum class MatchStatus { PENDING, ACCEPTED, REJECTED }

/** F15: vsebuje polje version za optimisticno zaklepanje pri socasnih ujemanjih. */
data class Match(
    val id: String,
    val userIdA: String,
    val userIdB: String,
    val status: MatchStatus,
    val version: Int
)

/** F14 */
data class Message(
    val id: String,
    val matchId: String,
    val senderId: String,
    val body: String,
    val deliveryStatus: DeliveryStatus
)

enum class DeliveryStatus { SENT, DELIVERED, READ }
