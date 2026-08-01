package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.MessageDto
import com.roomieslo.app.data.remote.dto.NewMessageDto
import com.roomieslo.app.domain.model.Message
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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
        supabase.from("messages").select {
            filter { eq("match_id", matchId) }
            order("sent_at", Order.ASCENDING)
        }.decodeList<MessageDto>().map { it.toDomain() }

    /** F14: poslji sporocilo v dano ujemanje. */
    suspend fun sendMessage(matchId: String, body: String) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("messages").insert(NewMessageDto(matchId = matchId, senderId = uid, body = body))
    }

    fun currentUserId(): String? = authRepository.currentUserId()
}
