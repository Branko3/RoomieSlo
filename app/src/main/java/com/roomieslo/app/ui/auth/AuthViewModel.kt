package com.roomieslo.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.AuthRepository
import com.roomieslo.app.ui.common.uporabnisko
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Stanje zaslonov za prijavo/registracijo. */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun signIn(email: String, password: String) = submit {
        authRepository.signIn(email.trim(), password)
    }

    fun signUp(name: String, email: String, password: String) = submit {
        authRepository.signUp(name.trim(), email.trim(), password)
    }

    /** UI potrdi, da je obravnaval uspeh (da se navigacija ne sprozi veckrat). */
    fun consumeSuccess() {
        uiState = uiState.copy(isSuccess = false)
    }

    private fun submit(block: suspend () -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                block()
                uiState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                // Pri prijavi je izvirno sporocilo koristno ("Invalid login credentials"),
                // zato ga obdrzimo; zamenjamo le napake omreznega sloja.
                uiState.copy(isLoading = false, errorMessage = e.uporabnisko(e.message ?: "Prišlo je do napake."))
            }
        }
    }
}
