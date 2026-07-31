package com.roomieslo.app.data.local.entity

import com.roomieslo.app.domain.model.Listing

fun ListingEntity.toDomain() = Listing(
    id = id,
    ownerId = ownerId,
    location = location,
    pricePerMonth = pricePerMonth,
    description = description,
    isFilled = isFilled
)

fun Listing.toEntity(syncedAt: Long = System.currentTimeMillis()) = ListingEntity(
    id = id,
    ownerId = ownerId,
    location = location,
    pricePerMonth = pricePerMonth,
    description = description,
    isFilled = isFilled,
    lastSyncedAt = syncedAt
)

