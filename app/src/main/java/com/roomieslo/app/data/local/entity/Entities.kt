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
    val lastSyncedAt: Long
)

@Entity(
    tableName = "listings",
    indices = [Index(value = ["isFilled", "pricePerMonth"])]
)
data class ListingEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val location: String,
    val pricePerMonth: Double,
    val description: String,
    val isFilled: Boolean,
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
