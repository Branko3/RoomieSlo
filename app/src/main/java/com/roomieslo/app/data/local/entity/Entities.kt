package com.roomieslo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val academicStatusVerified: Boolean,
    val isAvailable: Boolean,
    val lastSyncedAt: Long
)

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val location: String,
    val pricePerMonth: Double,
    val description: String,
    val isFilled: Boolean,
    val lastSyncedAt: Long
)

/** version: polje za opticno zaklepanje pri socasnih ujemanjih (glej diplomsko delo). */
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val userIdA: String,
    val userIdB: String,
    val status: String,
    val version: Int,
    val lastSyncedAt: Long
)
