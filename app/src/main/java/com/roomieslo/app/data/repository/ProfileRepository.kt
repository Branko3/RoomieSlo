package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.ProfileDto
import com.roomieslo.app.data.remote.dto.ProfileWithAnswersDto
import com.roomieslo.app.data.remote.dto.QuestionnaireAnswerDto
import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.domain.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

/** F03-F05, F12-F13: uporabniski profil, vprasalnik o zivljenjskem slogu, priporocila. */
@Singleton
class ProfileRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {

    /** F03: prebere profil trenutno prijavljenega uporabnika (z odgovori vprasalnika) prek PostgREST. */
    suspend fun getMyProfile(): Profile? {
        val uid = authRepository.currentUserId() ?: return null
        return supabase.from("profiles").select(PROFILE_WITH_ANSWERS) {
            filter { eq("id", uid) }
        }.decodeSingleOrNull<ProfileWithAnswersDto>()?.toDomain()
    }

    /** F03/F19: posodobi prikazano ime trenutnega uporabnika. */
    suspend fun updateDisplayName(name: String) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("profiles").update({ set("display_name", name) }) {
            filter { eq("id", uid) }
        }
    }

    /** F05: preklopi status razpolozljivosti (prikaz med priporocenimi zadetki). */
    suspend fun setAvailability(isAvailable: Boolean) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("profiles").update({ set("is_available", isAvailable) }) {
            filter { eq("id", uid) }
        }
    }

    /** F04: prebere odgovore vprasalnika za dani profil. */
    suspend fun getAnswersFor(profileId: String): List<LifestyleAnswer> =
        supabase.from("questionnaire_answers").select(Columns.list("profile_id", "question_id", "value", "weight")) {
            filter { eq("profile_id", profileId) }
        }.decodeList<QuestionnaireAnswerDto>().map { it.toDomain() }

    /** F04: odgovori vprasalnika trenutno prijavljenega uporabnika. */
    suspend fun getMyAnswers(): List<LifestyleAnswer> {
        val uid = authRepository.currentUserId() ?: return emptyList()
        return getAnswersFor(uid)
    }

    /** F04: shrani (vstavi ali posodobi) odgovore vprasalnika trenutnega uporabnika. */
    suspend fun saveMyAnswers(answers: List<LifestyleAnswer>) {
        val uid = authRepository.currentUserId() ?: return
        val dtos = answers.map {
            QuestionnaireAnswerDto(profileId = uid, questionId = it.questionId, value = it.value, weight = it.weight)
        }
        supabase.from("questionnaire_answers").upsert(dtos) { onConflict = "profile_id,question_id" }
    }

    /**
     * F12-F13: vsi razpolozljivi profili (razen mojega) skupaj z odgovori vprasalnika,
     * da lahko odjemalec izracuna zdruzljivost.
     *
     * Odgovori so vgnezdeni v isto zahtevo, zato zadostuje en obhod do streznika.
     */
    suspend fun getRecommendationCandidates(): List<Profile> {
        val uid = authRepository.currentUserId()
        return supabase.from("profiles").select(PROFILE_WITH_ANSWERS) {
            filter {
                eq("is_available", true)
                if (uid != null) neq("id", uid)
            }
        }.decodeList<ProfileWithAnswersDto>().map { it.toDomain() }
    }

    /** Prikazana imena za dani nabor id-jev (za seznam klepetov/ujemanj). */
    suspend fun getDisplayNames(ids: List<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        return supabase.from("profiles").select(Columns.list("id", "display_name")) {
            filter { isIn("id", ids) }
        }.decodeList<ProfileDto>().associate { it.id to it.displayName }
    }

    /**
     * Zavestna odlocitev: profili se v tej razlicici NE predpomnijo.
     *
     * Predpomnjenje z Room je izvedeno samo za oglase -- v ListingRepository.syncFromRemote().
     * Razlog: ProfileEntity ne hrani odgovorov vprasalnika (LifestyleAnswer), zato izracun
     * zdruzljivosti (F12/F13) brez povezave tako ali tako ne bi deloval. Za to bi bila
     * potrebna dodatna tabela in @Relation, kar presega obseg tega dela.
     */
    suspend fun syncFromRemote() {
        // Namenoma prazno.
    }

    companion object {
        /** Profil z vgnezdenimi odgovori vprasalnika v enem obhodu. */
        private val PROFILE_WITH_ANSWERS = Columns.raw(
            "id, display_name, academic_status_verified, is_available, " +
                "questionnaire_answers(profile_id, question_id, value, weight)"
        )
    }
}
