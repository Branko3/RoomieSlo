package com.roomieslo.app.data.realtime

import com.roomieslo.app.domain.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F14: real-time klepet prek WebSocket (Supabase Realtime).
 *
 * Zahtevnejsa funkcionalnost: logika ponovne vzpostavitve povezave (reconnect)
 * z eksponentnim odlogom med poskusi. Glej diplomsko delo, razdelek
 * "Real-time klepet z logiko ponovne vzpostavitve povezave".
 */
@Singleton
class ChatRealtimeService @Inject constructor() {

    private var reconnectAttempt = 0

    fun observeMessages(matchId: String): Flow<Message> = flow {
        while (true) {
            try {
                connectAndListen(matchId) { message -> emit(message) }
                reconnectAttempt = 0
            } catch (e: Exception) {
                val backoffMillis = minOf(30_000L, 1000L * (1 shl reconnectAttempt))
                reconnectAttempt++
                delay(backoffMillis)
            }
        }
    }

    private suspend fun connectAndListen(matchId: String, onMessage: suspend (Message) -> Unit) {
        // TODO: naroci se na Supabase Realtime kanal za dano ujemanje (matchId)
        // in za vsako prejeto sporocilo poklici onMessage(...).
    }

    suspend fun send(message: Message) {
        // TODO: poslji sporocilo prek Supabase Postgrest/Realtime in posodobi deliveryStatus.
    }
}
