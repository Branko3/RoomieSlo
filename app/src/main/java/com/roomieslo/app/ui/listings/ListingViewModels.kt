package com.roomieslo.app.ui.listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.FavoriteRepository
import com.roomieslo.app.data.repository.ListingRepository
import com.roomieslo.app.data.repository.MatchRepository
import com.roomieslo.app.domain.model.Listing
import com.roomieslo.app.ui.common.uporabnisko
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/* ---------- Seznam oglasov (F07) ---------- */

data class ListingListUiState(
    val isLoading: Boolean = true,
    val listings: List<Listing> = emptyList(),
    val errorMessage: String? = null,
    /** Ali se prenasa naslednja stran (za vrtiljak na dnu seznama). */
    val nalagaNaslednjo: Boolean = false,
    /** Nabor je izcrpan -- drsenje naj ne sprozi novih zahtev. */
    val jeKonec: Boolean = false
)

@HiltViewModel
class ListingListViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    var uiState by mutableStateOf(ListingListUiState())
        private set

    /** Koliko oglasov trenutno beremo iz predpomnilnika; raste ob drsenju do dna. */
    private val meja = MutableStateFlow(ListingRepository.VELIKOST_STRANI)

    /** Oznaka trenutnega obhoda po straneh -- glej ListingRepository.syncAllPage(). */
    private var oznaka = 0L

    init { opazujPredpomnilnik() }

    /**
     * Predpomnilnik opazujemo enkrat ob nastanku; Room sam odda nove podatke po vsaki
     * sinhronizaciji in ob vsaki spremembi meje, zato seznama ni treba nastavljati rocno.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun opazujPredpomnilnik() {
        viewModelScope.launch {
            meja.flatMapLatest { listingRepository.observeAllListings(it) }
                .collect { cached -> uiState = uiState.copy(isLoading = false, listings = cached) }
        }
    }

    /**
     * Osvezi s streznika. Ob napaki ostanejo prikazani predpomnjeni oglasi.
     *
     * Osvezi natanko toliko oglasov, kolikor jih zaslon trenutno prikazuje, in to v eni
     * zahtevi -- meje ne zmanjsamo, sicer bi seznam ob vrnitvi s podrobnosti skocil na vrh.
     */
    fun load() {
        uiState = uiState.copy(errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                oznaka = System.currentTimeMillis()
                val stran = listingRepository.syncAllPage(
                    createdBefore = null,
                    limit = meja.value,
                    oznaka = oznaka
                )
                uiState.copy(isLoading = false, jeKonec = stran.jeKonec)
            } catch (e: Exception) {
                uiState.copy(
                    isLoading = false,
                    // Ce predpomnilnik se ni prazen, napaka ni usodna -- povemo le, da so podatki shranjeni.
                    errorMessage = if (uiState.listings.isEmpty()) {
                        e.uporabnisko("Napaka pri nalaganju oglasov.")
                    } else {
                        "Ni povezave -- prikazani so shranjeni podatki."
                    }
                )
            }
        }
    }

    /**
     * Prenese naslednjo stran; klice jo zaslon, ko uporabnik pride do dna seznama.
     *
     * Kazalec vzamemo iz zadnjega prikazanega oglasa in ne iz odgovora streznika:
     * tako strancenje deluje tudi brez povezave, ko je predpomnilnik edini vir.
     * Iz istega razloga mejo razsirimo takoj, se pred zahtevo na streznik.
     */
    fun naloziNaslednjo() {
        if (uiState.nalagaNaslednjo || uiState.jeKonec) return
        val kazalec = uiState.listings.lastOrNull()?.createdAt ?: return
        uiState = uiState.copy(nalagaNaslednjo = true)
        meja.value += ListingRepository.VELIKOST_STRANI
        viewModelScope.launch {
            uiState = try {
                val stran = listingRepository.syncAllPage(
                    createdBefore = kazalec,
                    limit = ListingRepository.VELIKOST_STRANI,
                    oznaka = oznaka
                )
                uiState.copy(nalagaNaslednjo = false, jeKonec = stran.jeKonec)
            } catch (_: Exception) {
                // Brez povezave ostane razsirjena meja nad predpomnilnikom; konca nabora
                // ne moremo potrditi, zato jeKonec pustimo pri miru.
                uiState.copy(nalagaNaslednjo = false)
            }
        }
    }
}

/* ---------- Podrobnosti oglasa (F08, F09, F19) ---------- */

data class ListingDetailUiState(
    val isLoading: Boolean = true,
    val listing: Listing? = null,
    val isOwner: Boolean = false,
    val errorMessage: String? = null,
    val isDeleted: Boolean = false,
    val isRequestSent: Boolean = false,
    val isFavorite: Boolean = false
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val matchRepository: MatchRepository,
    private val favoriteRepository: FavoriteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listingId: String? = savedStateHandle["listingId"]

    var uiState by mutableStateOf(ListingDetailUiState())
        private set

    init { load() }

    fun load() {
        val id = listingId
        if (id == null) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Neveljaven oglas.")
            return
        }
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                val listing = listingRepository.getListing(id)
                uiState.copy(
                    isLoading = false,
                    listing = listing,
                    isOwner = listing != null && listing.ownerId == listingRepository.currentUserId(),
                    isFavorite = if (listing != null) favoriteRepository.isFavorite(listing.id) else false
                )
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.uporabnisko("Napaka pri nalaganju oglasa."))
            }
        }
    }

    /** F17: dodaj/odstrani oglas iz priljubljenih. */
    fun toggleFavorite() {
        val listing = uiState.listing ?: return
        val newValue = !uiState.isFavorite
        uiState = uiState.copy(isFavorite = newValue)
        viewModelScope.launch {
            try {
                if (newValue) favoriteRepository.addFavorite(listing.id)
                else favoriteRepository.removeFavorite(listing.id)
            } catch (e: Exception) {
                uiState = uiState.copy(isFavorite = !newValue, errorMessage = e.uporabnisko("Priljubljenih ni bilo mogoče posodobiti."))
            }
        }
    }

    fun toggleFilled() {
        val listing = uiState.listing ?: return
        val newValue = !listing.isFilled
        uiState = uiState.copy(listing = listing.copy(isFilled = newValue))
        viewModelScope.launch {
            try {
                listingRepository.markFilled(listing.id, newValue)
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.uporabnisko("Stanja oglasa ni bilo mogoče spremeniti."))
            }
        }
    }

    fun delete() {
        val listing = uiState.listing ?: return
        viewModelScope.launch {
            try {
                listingRepository.deleteListing(listing.id)
                uiState = uiState.copy(isDeleted = true)
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.uporabnisko("Oglasa ni bilo mogoče izbrisati."))
            }
        }
    }

    /** F15: poslji zahtevo za ujemanje lastniku oglasa. */
    fun sendMatchRequest() {
        val listing = uiState.listing ?: return
        if (uiState.isRequestSent) return
        uiState = uiState.copy(isRequestSent = true)
        viewModelScope.launch {
            try {
                matchRepository.sendRequest(listing.ownerId)
            } catch (e: Exception) {
                uiState = uiState.copy(isRequestSent = false, errorMessage = e.uporabnisko("Zahteve ni bilo mogoče poslati."))
            }
        }
    }
}

/* ---------- Ustvarjanje / urejanje oglasa (F06, F19) ---------- */

data class CreateListingUiState(
    val location: String = "",
    val price: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CreateListingViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editingId: String? = savedStateHandle["listingId"]
    val isEditing: Boolean get() = editingId != null

    var uiState by mutableStateOf(CreateListingUiState())
        private set

    init {
        if (editingId != null) {
            viewModelScope.launch {
                listingRepository.getListing(editingId)?.let { l ->
                    uiState = uiState.copy(
                        location = l.location,
                        price = l.pricePerMonth.toInt().toString(),
                        description = l.description
                    )
                }
            }
        }
    }

    fun onLocationChange(v: String) { uiState = uiState.copy(location = v) }
    fun onPriceChange(v: String) { uiState = uiState.copy(price = v.filter { it.isDigit() }) }
    fun onDescriptionChange(v: String) { uiState = uiState.copy(description = v) }

    fun save() {
        if (uiState.isSaving) return
        val price = uiState.price.toDoubleOrNull()
        if (uiState.location.isBlank() || price == null) {
            uiState = uiState.copy(errorMessage = "Vnesi lokacijo in ceno.")
            return
        }
        uiState = uiState.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                if (editingId != null) {
                    listingRepository.updateListing(editingId, uiState.location, price, uiState.description)
                } else {
                    listingRepository.createListing(uiState.location, price, uiState.description)
                }
                uiState.copy(isSaving = false, isSaved = true)
            } catch (e: Exception) {
                uiState.copy(isSaving = false, errorMessage = e.uporabnisko("Shranjevanje ni uspelo."))
            }
        }
    }
}
