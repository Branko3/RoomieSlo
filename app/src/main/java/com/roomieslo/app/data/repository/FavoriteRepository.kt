package com.roomieslo.app.data.repository

import com.roomieslo.app.data.remote.dto.FavoriteDto
import com.roomieslo.app.data.remote.dto.ListingDto
import com.roomieslo.app.data.remote.dto.NewFavoriteDto
import com.roomieslo.app.domain.model.Listing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

/** F17: shranjevanje oglasov med priljubljene (tabela `favorites`). */
@Singleton
class FavoriteRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository
) {

    suspend fun addFavorite(listingId: String) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("favorites").insert(NewFavoriteDto(profileId = uid, listingId = listingId))
    }

    suspend fun removeFavorite(listingId: String) {
        val uid = authRepository.currentUserId() ?: return
        supabase.from("favorites").delete {
            filter {
                eq("profile_id", uid)
                eq("listing_id", listingId)
            }
        }
    }

    suspend fun isFavorite(listingId: String): Boolean {
        val uid = authRepository.currentUserId() ?: return false
        return supabase.from("favorites").select(Columns.list("listing_id")) {
            filter {
                eq("profile_id", uid)
                eq("listing_id", listingId)
            }
        }.decodeList<FavoriteDto>().isNotEmpty()
    }

    /** F17: priljubljeni oglasi trenutnega uporabnika. */
    suspend fun getMyFavorites(): List<Listing> {
        val uid = authRepository.currentUserId() ?: return emptyList()
        val ids = supabase.from("favorites").select(Columns.list("listing_id")) {
            filter { eq("profile_id", uid) }
        }.decodeList<FavoriteDto>().map { it.listingId }
        if (ids.isEmpty()) return emptyList()
        return supabase.from("listings").select {
            filter { isIn("id", ids) }
        }.decodeList<ListingDto>().map { it.toDomain() }
    }
}
