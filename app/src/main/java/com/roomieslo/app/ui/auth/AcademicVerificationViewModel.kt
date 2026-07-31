package com.roomieslo.app.ui.auth

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Stanje zaslona za nalaganje potrdila o vpisu (F02). */
data class AcademicVerificationUiState(
    val fileName: String? = null,
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AcademicVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var uiState by mutableStateOf(AcademicVerificationUiState())
        private set

    /** F02: preberi vsebino izbrane datoteke in jo naloži v bucket `vpisnice`. */
    fun upload(uri: Uri) {
        uiState = uiState.copy(isUploading = true, isUploaded = false, errorMessage = null, fileName = fileNameOf(uri))
        viewModelScope.launch {
            uiState = try {
                val mime = context.contentResolver.getType(uri)
                val extension = when (mime) {
                    "application/pdf" -> "pdf"
                    "image/png" -> "png"
                    else -> "jpg"
                }
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw IllegalStateException("Datoteke ni bilo mogoče prebrati.")
                authRepository.uploadAcademicProof(bytes, extension)
                uiState.copy(isUploading = false, isUploaded = true)
            } catch (e: Exception) {
                uiState.copy(isUploading = false, errorMessage = e.message ?: "Nalaganje ni uspelo.")
            }
        }
    }

    private fun fileNameOf(uri: Uri): String =
        uri.lastPathSegment?.substringAfterLast('/') ?: "vpisnica"
}
