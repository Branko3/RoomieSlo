package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.MessageDto
import com.roomieslo.app.data.remote.dto.NewMessageDto
import com.roomieslo.app.domain.model.Message
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/**
 * F14: klepet med udelezencema ujemanja.
 *
 * Posiljanje in zacetno branje potekata prek PostgREST, sprejem novih sporocil
 * v realnem casu pa prek WebSocket -- znotraj ChatRealtimeService.
 */
@Singleton
class ChatRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {

    /** F14: vsa sporocila za dano ujemanje, urejena po casu. */
    suspend fun getMessages(matchId: String): List<Message> =
        supabase.from("messages").select(MESSAGE_COLUMNS) {
            filter { eq("match_id", matchId) }
            order("sent_at", Order.ASCENDING)
        }.decodeList<MessageDto>().map { it.toDomain() }

    /**
     * F14: poslji sporocilo v dano ujemanje.
     *
     * Vrne vstavljeno sporocilo, da ga zaslon lahko doda neposredno v seznam --
     * brez tega bi moral po vsakem poslanem sporocilu ponovno prenesti cel klepet.
     */
    suspend fun sendMessage(matchId: String, body: String): Message? {
        val uid = authRepository.currentUserId() ?: return null
        return supabase.from("messages").insert(
            NewMessageDto(matchId = matchId, senderId = uid, body = body)
        ) {
            select(MESSAGE_COLUMNS)
        }.decodeSingleOrNull<MessageDto>()?.toDomain()
    }

    fun currentUserId(): String? = authRepository.currentUserId()

    companion object {
        private val MESSAGE_COLUMNS = Columns.list("id", "match_id", "sender_id", "body", "delivery_status")
    }
}
