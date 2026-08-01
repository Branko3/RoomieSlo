package com.roomieslo.app.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.ListingRepository
import com.roomieslo.app.data.repository.ProfileRepository
import com.roomieslo.app.domain.model.Listing
import com.roomieslo.app.domain.model.Profile
import com.roomieslo.app.domain.usecase.CalculateCompatibilityScoreUseCase
import com.roomieslo.app.ui.common.uporabnisko
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import javax.inject.Inject

/* ---------- Iskanje oglasov po lokaciji in proracunu (F10, F11) ---------- */

data class SearchUiState(
    val location: String = "",
    val minPrice: Float = 200f,
    val maxPrice: Float = 500f,
    val results: List<Listing> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set
    /** Opazovalec predpomnilnika; ob novem iskanju ga prekinemo, da ne teceta dva hkrati. */
    private var observeJob: Job? = null

    fun onLocationChange(v: String) { uiState = uiState.copy(location = v) }

    fun onPriceRangeChange(range: ClosedFloatingPointRange<Float>) {
        uiState = uiState.copy(minPrice = range.start, maxPrice = range.endInclusive)
    }

    /**
     * F10/F11: iskanje po lokaciji in proracunu, offline-first.
     *
     * Predpomnilnik (Room) se prikaze takoj, streznik pa ga osvezi v ozadju.
     * Spodnja meja cene se filtrira lokalno.
     */
    fun search() {
        // Zajamemo trenutne filtre: opazovalec zivi dlje, uiState se medtem lahko spremeni.
        val loc = uiState.location.trim()
        val min = uiState.minPrice
        // Zgornji polozaj drsnika pomeni "brez zgornje meje": Room dobi neomejeno
        // vrednost, streznik pa null, da filtra cene sploh ne doda.
        val brezMeje = uiState.maxPrice >= NAJVECJA_CENA
        val max = if (brezMeje) Double.MAX_VALUE else uiState.maxPrice.toDouble()

        uiState = uiState.copy(isLoading = true, hasSearched = true, errorMessage = null)

        // 1) Opazuj predpomnilnik -- UI se napolni takoj, tudi brez povezave.
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            listingRepository.observeListings(loc, max).collect { cached ->
                uiState = uiState.copy(results = cached.filter { it.pricePerMonth >= min })
            }
        }

        // 2) Osvezi s streznika. Room Flow zgoraj sam odda nove podatke,
        //    zato tu rezultatov ni treba nastavljati rocno.
        viewModelScope.launch {
            uiState = try {
                listingRepository.syncFromRemote(loc.ifBlank { null }, if (brezMeje) null else max)
                uiState.copy(isLoading = false)
            } catch (_: Exception) {
                // Vzrok namerno zavrzemo: predpomnilnik je ze prikazan, zato je
                // za uporabnika pomembno le, da podatki niso sveži.
                uiState.copy(
                    isLoading = false,
                    errorMessage = "Ni povezave -- prikazani so shranjeni podatki."
                )
            }
        }
    }

    companion object {
        /** Zgornji polozaj drsnika; pomeni iskanje brez zgornje meje cene. */
        const val NAJVECJA_CENA = 800f
    }
}

/* ---------- Priporoceni sostanovalci po zdruzljivosti (F12, F13) ---------- */

data class RecommendedUiState(
    val isLoading: Boolean = true,
    val results: List<Pair<Profile, Int>> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class RecommendedProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val calculateCompatibility: CalculateCompatibilityScoreUseCase
) : ViewModel() {

    var uiState by mutableStateOf(RecommendedUiState())
        private set

    init { load() }

    fun load() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                val me = profileRepository.getMyProfile()
                val candidates = profileRepository.getRecommendationCandidates()
                // F12: izracun zdruzljivosti; F13: razvrstitev padajoce po rezultatu.
                val scored = candidates.map { candidate ->
                    val score = if (me != null) (calculateCompatibility(me, candidate) * 100).roundToInt() else 0
                    candidate to score
                }.sortedByDescending { it.second }
                uiState.copy(isLoading = false, results = scored)
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.uporabnisko("Napaka pri nalaganju priporocil."))
            }
        }
    }
}
