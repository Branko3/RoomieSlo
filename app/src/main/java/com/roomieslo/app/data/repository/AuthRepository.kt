package com.roomieslo.app.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/** F01: registracija in prijava. F02: preverjanje akademskega statusa (potrdilo o vpisu / upisnica). */
@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {

    /**
     * F01: registracija prek Supabase Auth. Ime shranimo kot metapodatek uporabnika;
     * vrstico v tabeli profiles samodejno ustvari sprožilec handle_new_user (glej supabase/auth_trigger.sql).
     */
    suspend fun signUp(name: String, email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject { put("display_name", name) }
        }
    }

    /** F01: prijava prek Supabase Auth. Ob uspehu se seja shrani v odjemalcu. */
    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Stanje seje (nalaganje ob zagonu / prijavljen / neprijavljen). Uporablja se ob zagonu
     * aplikacije za odlocitev, ali uporabnika usmerimo na prijavo ali naravnost na oglase.
     */
    val sessionStatus: StateFlow<SessionStatus> = supabase.auth.sessionStatus

    /** Trenutno prijavljeni uporabnik (id) ali null. */
    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    /**
     * F02: naloži potrdilo o vpisu (vpisnico) v zaseben bucket `vpisnice`.
     * Datoteka se shrani pod potjo `{uid}/vpisnica.ext`, tako da je prvi imenik = lastnik
     * (glej supabase/storage_policies.sql). Preverjanje statusa opravi administrator.
     */
    suspend fun uploadAcademicProof(bytes: ByteArray, extension: String) {
        val uid = currentUserId() ?: return
        supabase.storage.from("vpisnice").upload("$uid/vpisnica.$extension", bytes) {
            upsert = true
        }
    }
}
