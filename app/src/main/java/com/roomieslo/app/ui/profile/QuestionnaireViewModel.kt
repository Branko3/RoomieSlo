package com.roomieslo.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.ProfileRepository
import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.domain.model.LikertOdgovor
import com.roomieslo.app.domain.model.Trditev
import com.roomieslo.app.domain.model.Vprasalnik
import com.roomieslo.app.ui.common.uporabnisko
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Stanje zaslona vprasalnika (F04). */
data class QuestionnaireUiState(
    val isLoading: Boolean = true,
    /** Izbrane stopnje po id-ju trditve; trditve brez vnosa uporabnik se ni odgovoril. */
    val odgovori: Map<String, LikertOdgovor> = emptyMap(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    /** Stevilo trditev, pri katerih je uporabnik izrekel stopnjo strinjanja. */
    val steviloIzrecenih: Int get() = odgovori.count { it.value.jeIzrecen }
}

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /** Nabor trditev je fiksen (glej Vprasalnik). */
    val trditve: List<Trditev> = Vprasalnik.trditve

    var uiState by mutableStateOf(QuestionnaireUiState())
        private set

    init { load() }

    /**
     * Prebere ze shranjene odgovore, da lahko uporabnik vprasalnik kadar koli izpolni znova.
     *
     * Trditve brez shranjenega odgovora ostanejo neizbrane -- uporabnik tako vidi, katerih
     * se ni odgovoril, namesto da bi mu privzeta stopnja izgledala kot njegova izbira.
     */
    private fun load() {
        viewModelScope.launch {
            uiState = try {
                val shranjeni = profileRepository.getMyAnswers().associateBy { it.questionId }
                val odgovori = trditve.mapNotNull { trditev ->
                    shranjeni[trditev.id]?.let { odgovor ->
                        trditev.id to LikertOdgovor.izVrednosti(odgovor.value, odgovor.weight)
                    }
                }.toMap()
                uiState.copy(isLoading = false, odgovori = odgovori)
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.uporabnisko("Napaka pri nalaganju vprasalnika."))
            }
        }
    }

    fun izberi(idTrditve: String, odgovor: LikertOdgovor) {
        uiState = uiState.copy(odgovori = uiState.odgovori + (idTrditve to odgovor))
    }

    /**
     * Shrani odgovore na vse trditve naenkrat.
     *
     * Trditve, ki jih uporabnik ni izbral, se zapisejo kot [LikertOdgovor.NE_ZELIM_ODGOVORITI]:
     * v bazi tako ni manjkajocih vrstic, utez 0 pa poskrbi, da neizrecen odgovor ne vpliva
     * na izracun zdruzljivosti.
     *
     * Isti klic uporabi tudi gumb za preskok vprasalnika -- preskok pomeni le, da so vse
     * preostale trditve ostale neodgovorjene.
     */
    fun shrani() {
        if (uiState.isSaving) return
        uiState = uiState.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                val odgovori = trditve.map { trditev ->
                    val izbran = uiState.odgovori[trditev.id] ?: LikertOdgovor.NE_ZELIM_ODGOVORITI
                    LifestyleAnswer(
                        questionId = trditev.id,
                        value = izbran.vrednost,
                        weight = trditev.utez * izbran.mnozitelj
                    )
                }
                profileRepository.saveMyAnswers(odgovori)
                uiState.copy(isSaving = false, isSaved = true)
            } catch (e: Exception) {
                uiState.copy(isSaving = false, errorMessage = e.uporabnisko("Shranjevanje ni uspelo."))
            }
        }
    }
}
