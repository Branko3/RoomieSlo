package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.AdminDto
import com.roomieslo.app.data.remote.dto.NewReportDto
import com.roomieslo.app.data.remote.dto.ReportDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/** F18: prijava neprimernega uporabnika. F20: administratorska plošča. */
@Singleton
class AdminRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {

    /** F18: ustvari prijavo (reporter_id = trenutni uporabnik). */
    suspend fun reportUser(reportedUserId: String, reason: String, description: String) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("reports").insert(
            NewReportDto(reporterId = uid, reportedId = reportedUserId, reason = reason, description = description)
        )
    }

    /** Ali je trenutni uporabnik administrator (obstaja vrstica v `admins`). */
    suspend fun isAdmin(): Boolean {
        val uid = authRepository.currentUserId() ?: return false
        return supabase.from("admins").select(Columns.list("user_id")) {
            filter { eq("user_id", uid) }
        }.decodeList<AdminDto>().isNotEmpty()
    }

    /** F20: seznam prijav (RLS dovoli branje le administratorjem). */
    suspend fun listReports(): List<ReportDto> =
        supabase.from("reports").select {
            order("created_at", Order.DESCENDING)
        }.decodeList<ReportDto>()

    /** F20: označi prijavo kot obravnavano / ponovno odpri. */
    suspend fun setReportStatus(reportId: String, status: String) {
        supabase.from("reports").update({ set("status", status) }) { filter { eq("id", reportId) } }
    }
}
