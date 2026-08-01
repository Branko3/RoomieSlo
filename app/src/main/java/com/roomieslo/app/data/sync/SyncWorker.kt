package com.roomieslo.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.roomieslo.app.data.repository.ListingRepository
import com.roomieslo.app.data.repository.MatchRepository
import com.roomieslo.app.data.repository.ProfileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Zahtevnejsa funkcionalnost: asinhrona sinhronizacija z WorkManager.
 * Poganja se periodicno in ob dogodkih (npr. sprememba omreznega stanja), tudi ko
 * aplikacija ni v ospredju. Glej diplomsko delo, razdelek "Asinhrona sinhronizacija z WorkManager".
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
    private val listingRepository: ListingRepository,
    private val matchRepository: MatchRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            profileRepository.syncFromRemote()
            listingRepository.syncAllFromRemote()
            matchRepository.syncFromRemote()
            Result.success()
        } catch (e: Exception) {
            Result.retry() // eksponentni odlog konfiguriran ob vlozitvi opravila
        }
    }

    companion object {
        const val WORK_NAME = "roomieslo_sync"
    }
}
