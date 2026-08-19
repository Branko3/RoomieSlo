package com.roomieslo.app.data.local.entity

import com.roomieslo.app.domain.model.Listing

fun ListingEntity.toDomain() = Listing(
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

fun Listing.toEntity(syncedAt: Long = System.currentTimeMillis()) = ListingEntity(
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
    photoUrl = photoUrl,
    lastSyncedAt = syncedAt
)
