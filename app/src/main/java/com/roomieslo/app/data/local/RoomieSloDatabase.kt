package com.roomieslo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.roomieslo.app.data.local.dao.ListingDao
import com.roomieslo.app.data.local.dao.MatchDao
import com.roomieslo.app.data.local.dao.ProfileDao
import com.roomieslo.app.data.local.entity.ListingEntity
import com.roomieslo.app.data.local.entity.MatchEntity
import com.roomieslo.app.data.local.entity.ProfileEntity

@Database(
    entities = [ProfileEntity::class, ListingEntity::class, MatchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RoomieSloDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun listingDao(): ListingDao
    abstract fun matchDao(): MatchDao
}
