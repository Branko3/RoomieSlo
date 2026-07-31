package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.MatchDto
import com.roomieslo.app.data.remote.dto.NewMatchDto
import com.roomieslo.app.domain.model.Match
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AcceptMatchResult {
    data object Success : AcceptMatchResult
    data object Conflict : AcceptMatchResult
}

/**
 * F15: zahteva za ujemanje (poslji / sprejmi / zavrni).
 *
 * Opomba: opticno zaklepanje nad poljem `version` (napredna tehnika) v tej razlicici
 * ni izvedeno — sprejem je preprosta posodobitev statusa.
 */
@Singleton
class MatchRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {

    /** F15: poslji zahtevo za ujemanje uporabniku (npr. lastniku oglasa). */
    suspend fun sendRequest(toUserId: String) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("matches").insert(NewMatchDto(userIdA = uid, userIdB = toUserId))
    }

    /** Vsa ujemanja, v katerih sodeluje trenutni uporabnik. */
    suspend fun getMyMatches(): List<Match> {
        val uid = authRepository.currentUserId() ?: return emptyList()
        return supabase.from("matches").select {
            filter {
                or {
                    eq("user_id_a", uid)
                    eq("user_id_b", uid)
                }
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<MatchDto>().map { it.toDomain() }
    }

    /** F15: sprejmi zahtevo za ujemanje (odpre klepet). */
    suspend fun acceptMatch(matchId: String): AcceptMatchResult {
       val updated = supabase.from("matches").update({ set("status", "accepted") }) { filter {
               eq("id", matchId)
               eq("status", "pending")
           }
           select()
       }.decodeList<MatchDto>()

        return if (updated.isEmpty()) AcceptMatchResult.Conflict else AcceptMatchResult.Success

    }

    /** F15: zavrni zahtevo za ujemanje. */
    suspend fun rejectMatch(matchId: String) {
        supabase.from("matches").update({ set("status", "rejected") }) { filter {
                eq("id", matchId)
                eq("status", "pending")
            }
        }

    }

    fun currentUserId(): String? = authRepository.currentUserId()

    /**
     * Zavestna odlocitev: ujemanja se v tej razlicici NE predpomnijo.
     *
     * Predpomnjenje z Room je izvedeno samo za oglase -- v ListingRepository.syncFromRemote().
     * Razlog: stanje ujemanja se pogosto spreminja in mora biti sveze -- acceptMatch() se
     * zanasa na to, da je vir resnice streznik (pogojna posodobitev status = 'pending').
     * Predpomnjen status bi lahko prikazal zahtevo, ki jo je druga stran ze obravnavala.
     */
    suspend fun syncFromRemote() {
        // Namenoma prazno.
    }
}
