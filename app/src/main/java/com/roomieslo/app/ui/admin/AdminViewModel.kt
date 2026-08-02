package com.roomieslo.app.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.remote.dto.ReportDto
import com.roomieslo.app.data.repository.AdminRepository
import com.roomieslo.app.ui.common.uporabnisko
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val reports: List<ReportDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    var uiState by mutableStateOf(AdminUiState())
        private set

    init { load() }

    fun load() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                val admin = adminRepository.isAdmin()
                val reports = if (admin) adminRepository.listReports() else emptyList()
                uiState.copy(isLoading = false, isAdmin = admin, reports = reports)
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.uporabnisko("Napaka pri nalaganju prijav."))
            }
        }
    }

    /** F20: preklopi status prijave (odprto <-> obravnavano). */
    fun toggleStatus(report: ReportDto) {
        val newStatus = if (report.status == "open") "reviewed" else "open"
        viewModelScope.launch {
            try {
                adminRepository.setReportStatus(report.id, newStatus)
                // Spremenila se je ena vrstica, zato posodobimo samo njo -- ponovno
                // branje celotnega seznama prijav tu ni potrebno.
                uiState = uiState.copy(
                    reports = uiState.reports.map {
                        if (it.id == report.id) it.copy(status = newStatus) else it
                    }
                )
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.uporabnisko("Statusa prijave ni bilo mogoce spremeniti."))
            }
        }
    }
}
