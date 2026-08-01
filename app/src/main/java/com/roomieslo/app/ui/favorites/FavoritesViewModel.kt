package com.roomieslo.app.ui.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.FavoriteRepository
import com.roomieslo.app.domain.model.Listing
import com.roomieslo.app.ui.common.uporabnisko
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favorites: List<Listing> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    var uiState by mutableStateOf(FavoritesUiState())
        private set

    init { load() }

    fun load() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                uiState.copy(isLoading = false, favorites = favoriteRepository.getMyFavorites())
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.uporabnisko("Napaka pri nalaganju priljubljenih."))
            }
        }
    }

    fun remove(listingId: String) {
        uiState = uiState.copy(favorites = uiState.favorites.filterNot { it.id == listingId })
        viewModelScope.launch {
            try {
                favoriteRepository.removeFavorite(listingId)
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.uporabnisko("Priljubljenega ni bilo mogoce odstraniti."))
            }
        }
    }
}
