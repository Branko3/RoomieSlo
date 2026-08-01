package com.roomieslo.app.di

import android.content.Context
import androidx.room.Room
import com.roomieslo.app.data.local.RoomieSloDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** F: lokalno predpomnjenje z Room -- glej diplomsko delo, poglavje Implementacija. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RoomieSloDatabase =
        Room.databaseBuilder(context, RoomieSloDatabase::class.java, "roomieslo.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideProfileDao(db: RoomieSloDatabase) = db.profileDao()

    @Provides
    fun provideListingDao(db: RoomieSloDatabase) = db.listingDao()

    @Provides
    fun provideMatchDao(db: RoomieSloDatabase) = db.matchDao()
}
