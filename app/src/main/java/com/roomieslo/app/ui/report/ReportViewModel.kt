package com.roomieslo.app.ui.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val reason: String = "Neprimerna vsebina",
    val description: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportedUserId: String? = savedStateHandle["userId"]

    var uiState by mutableStateOf(ReportUiState())
        private set

    fun onReasonChange(v: String) { uiState = uiState.copy(reason = v) }
    fun onDescriptionChange(v: String) { uiState = uiState.copy(description = v) }

    fun submit() {
        val target = reportedUserId
        if (target.isNullOrBlank()) {
            uiState = uiState.copy(errorMessage = "Neveljaven uporabnik.")
            return
        }
        if (uiState.isSubmitting) return
        uiState = uiState.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                adminRepository.reportUser(target, uiState.reason, uiState.description)
                uiState.copy(isSubmitting = false, isSubmitted = true)
            } catch (e: Exception) {
                uiState.copy(isSubmitting = false, errorMessage = e.message ?: "Prijava ni bila poslana.")
            }
        }
    }
}
