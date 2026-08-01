package com.roomieslo.app.data.realtime

import com.roomieslo.app.data.remote.dto.MessageDto
import com.roomieslo.app.domain.model.Message
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F14: klepet v realnem casu prek WebSocket (Supabase Realtime).
 *
 * Napredna tehnika: ponovna vzpostavitev povezave z eksponentnim odlogom.
 * Ob prekinjeni povezavi se poskus ponovi po 1, 2, 4, 8, 16 in nato 30 sekundah;
 * ob uspesni narocnini se stevec ponastavi.
 */
@Singleton
class ChatRealtimeService @Inject constructor(
    private val supabase: SupabaseClient
) {

    /** Tok novih sporocil za dano ujemanje. Tok se ne konca -- zivi, dokler ga zbiralec ne preklice. */
    fun observeMessages(matchId: String): Flow<Message> = flow {
        // Stevec je lokalen: en klepet ne sme vplivati na odlog drugega.
        var poskus = 0

        while (true) {
            try {
                val kanal = supabase.channel("klepet-$matchId")

                // Tok je treba ustvariti pred subscribe(), sicer narocnina ne zajame sprememb.
                val spremembe = kanal.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "messages"
                    filter("match_id", FilterOperator.EQ, matchId)
                }

                kanal.subscribe(blockUntilSubscribed = true)
                poskus = 0 // povezava uspela -> odlog se ponastavi

                try {
                    spremembe.collect { akcija ->
                        emit(akcija.decodeRecord<MessageDto>().toDomain())
                    }
                } finally {
                    // NonCancellable: odjava mora uspeti tudi, ce je zbiralec ravno preklican.
                    withContext(NonCancellable) { kanal.unsubscribe() }
                }
            } catch (e: CancellationException) {
                // Preklic ni napaka -- brez tega se tok ne bi nikoli ustavil.
                throw e
            } catch (e: Exception) {
                val odlog = (OSNOVNI_ODLOG_MS shl minOf(poskus, NAJVECJI_POMIK))
                    .coerceAtMost(NAJVECJI_ODLOG_MS)
                poskus++
                delay(odlog.milliseconds)
            }
        }
    }

    companion object {
        private const val OSNOVNI_ODLOG_MS = 1_000L
        private const val NAJVECJI_ODLOG_MS = 30_000L
        private const val NAJVECJI_POMIK = 5
    }
}