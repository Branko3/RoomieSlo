package com.roomieslo.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.AdminRepository
import com.roomieslo.app.data.repository.AuthRepository
import com.roomieslo.app.data.repository.ProfileRepository
import com.roomieslo.app.domain.model.Profile
import com.roomieslo.app.ui.common.uporabnisko
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Stanje zaslona Profil (F03/F05). */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val errorMessage: String? = null,
    val isSignedOut: Boolean = false,
    /**
     * Ali ima prijavljeni uporabnik vrstico v tabeli `admins` (F20).
     *
     * Privzeto false: vstop v administratorsko plosco je treba pokazati sele, ko je
     * pravica potrjena, in ne dokler se preverja.
     */
    val isAdmin: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init { load() }

    /** F03: nalozi profil prijavljenega uporabnika iz baze. */
    fun load() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                // Pravico preverimo ob vsakem nalaganju profila, ne enkrat ob prijavi:
                // vrstico v `admins` lahko kdo doda ali odstrani med sejo.
                val jeAdmin = runCatching { adminRepository.isAdmin() }.getOrDefault(false)
                uiState.copy(
                    isLoading = false,
                    profile = profileRepository.getMyProfile(),
                    isAdmin = jeAdmin
                )
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.uporabnisko("Napaka pri nalaganju profila."))
            }
        }
    }

    /** F03/F19: shrani novo prikazano ime (optimisticno posodobi UI, nato zapisi v bazo). */
    fun updateDisplayName(name: String) {
        val current = uiState.profile ?: return
        uiState = uiState.copy(profile = current.copy(displayName = name.trim()))
        viewModelScope.launch {
            try {
                profileRepository.updateDisplayName(name.trim())
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.uporabnisko("Shranjevanje ni uspelo."))
            }
        }
    }

    /** F05: preklopi razpolozljivost (optimisticno + zapis v bazo). */
    fun setAvailability(isAvailable: Boolean) {
        val current = uiState.profile ?: return
        uiState = uiState.copy(profile = current.copy(isAvailable = isAvailable))
        viewModelScope.launch {
            try {
                profileRepository.setAvailability(isAvailable)
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.uporabnisko("Shranjevanje ni uspelo."))
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (_: Exception) {
                // tudi ob napaki lokalno odjavimo uporabnika
            }
            uiState = uiState.copy(isSignedOut = true)
        }
    }
}
