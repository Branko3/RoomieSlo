package com.roomieslo.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val academicStatusVerified: Boolean,
    val isAvailable: Boolean,
    val age: Int? = null,
    val faculty: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val lastSyncedAt: Long
)

@Entity(
    tableName = "listings",
    indices = [
        Index(value = ["isFilled", "pricePerMonth"]),
        // Seznam oglasov bere po vrstnem redu nastanka in po straneh.
        Index(value = ["isFilled", "createdAt"])
    ]
)
data class ListingEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val location: String,
    val pricePerMonth: Double,
    val description: String,
    val isFilled: Boolean,
    /** ISO 8601, enako kot na strezniku -- niz se v tem zapisu pravilno primerja tudi leksikografsko. */
    val createdAt: String,
    // Polja iz migracije 0002. Predpomnilnik jih hrani, da je kartica v seznamu polna
    // tudi brez omrezne povezave -- sicer bi oglas brez povezave izgubil vecino prikaza.
    val title: String = "",
    val roomType: String = "",
    val district: String = "",
    val availableFrom: String? = null,
    val sizeSqm: Int? = null,
    val deposit: Double? = null,
    val billsIncluded: Boolean = false,
    val furnished: Boolean = false,
    val flatmatesCount: Int = 0,
    val photoUrl: String = "",
    val lastSyncedAt: Long
)

/**
 * Polje `version` je rezervirano za optimisticno zaklepanje z verzioniranjem, a se trenutno
 * ne uporablja: MatchRepository.acceptMatch() dosega enak ucinek s pogojnim UPDATE
 * na polju `status`, brez dodatnega stevca. Tabela se zaenkrat tudi ne predpomni.
 */
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val userIdA: String,
    val userIdB: String,
    val status: String,
    val version: Int,
    val lastSyncedAt: Long
)
