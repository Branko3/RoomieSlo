package com.roomieslo.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.ProfileRepository
import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.ui.common.SampleData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Stanje zaslona vprašalnika (F04). */
data class QuestionnaireUiState(
    val isLoading: Boolean = true,
    val values: Map<String, Float> = emptyMap(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /** Pari (besedilo vprašanja, id vprašanja) — nabor vprašanj je fiksen. */
    val questions: List<Pair<String, String>> = SampleData.lifestyleQuestions

    var uiState by mutableStateOf(QuestionnaireUiState())
        private set

    init { load() }

    private fun load() {
        viewModelScope.launch {
            uiState = try {
                val existing = profileRepository.getMyAnswers().associate { it.questionId to it.value }
                // manjkajoča vprašanja dobijo nevtralno privzeto vrednost 0.5
                val values = questions.associate { (_, id) -> id to (existing[id] ?: 0.5f) }
                uiState.copy(isLoading = false, values = values)
            } catch (e: Exception) {
                val values = questions.associate { (_, id) -> id to 0.5f }
                uiState.copy(isLoading = false, values = values, errorMessage = e.message)
            }
        }
    }

    fun setValue(questionId: String, value: Float) {
        uiState = uiState.copy(values = uiState.values + (questionId to value))
    }

    fun save() {
        if (uiState.isSaving) return
        uiState = uiState.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                val answers = uiState.values.map { (id, value) ->
                    LifestyleAnswer(questionId = id, value = value, weight = 1f)
                }
                profileRepository.saveMyAnswers(answers)
                uiState.copy(isSaving = false, isSaved = true)
            } catch (e: Exception) {
                uiState.copy(isSaving = false, errorMessage = e.message ?: "Shranjevanje ni uspelo.")
            }
        }
    }
}
