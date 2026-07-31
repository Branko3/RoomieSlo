package com.roomieslo.app.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomieslo.app.data.repository.AcceptMatchResult
import com.roomieslo.app.data.repository.ChatRepository
import com.roomieslo.app.data.repository.MatchRepository
import com.roomieslo.app.data.repository.ProfileRepository
import com.roomieslo.app.domain.model.Match
import com.roomieslo.app.domain.model.MatchStatus
import com.roomieslo.app.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/* ---------- Seznam ujemanj / klepetov (F15) ---------- */

data class MatchRow(
    val match: Match,
    val peerName: String,
    val iAmRecipient: Boolean
)

data class ChatListUiState(
    val isLoading: Boolean = true,
    val rows: List<MatchRow> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var uiState by mutableStateOf(ChatListUiState())
        private set

    init { load() }

    fun load() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            uiState = try {
                val uid = matchRepository.currentUserId()
                val matches = matchRepository.getMyMatches()
                val peerIds = matches.map { if (it.userIdA == uid) it.userIdB else it.userIdA }
                val names = profileRepository.getDisplayNames(peerIds)
                val rows = matches.map { m ->
                    val peerId = if (m.userIdA == uid) m.userIdB else m.userIdA
                    MatchRow(
                        match = m,
                        peerName = names[peerId]?.ifBlank { "Uporabnik" } ?: "Uporabnik",
                        iAmRecipient = m.userIdB == uid && m.status == MatchStatus.PENDING
                    )
                }
                uiState.copy(isLoading = false, rows = rows)
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.message ?: "Napaka pri nalaganju ujemanj.")
            }
        }
    }

    fun accept(matchId: String) {
        viewModelScope.launch {
            try {
                val result = matchRepository.acceptMatch(matchId)
                load()
                // load() pocisti errorMessage, zato sporocilo nastavimo sele za njim.
                if (result is AcceptMatchResult.Conflict) {
                    uiState = uiState.copy(errorMessage = "Ta zahteva je bila že obravnavana.")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }

    fun reject(matchId: String) {
        viewModelScope.launch {
            try {
                matchRepository.rejectMatch(matchId)
                load()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }
}

/* ---------- Klepet posameznega ujemanja (F14) ---------- */

data class ChatUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val myUserId: String? = null,
    val draft: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: String? = savedStateHandle["matchId"]

    var uiState by mutableStateOf(ChatUiState(myUserId = chatRepository.currentUserId()))
        private set

    init { load() }

    fun load() {
        val id = matchId ?: run {
            uiState = uiState.copy(isLoading = false, errorMessage = "Neveljavno ujemanje.")
            return
        }
        viewModelScope.launch {
            uiState = try {
                uiState.copy(isLoading = false, messages = chatRepository.getMessages(id))
            } catch (e: Exception) {
                uiState.copy(isLoading = false, errorMessage = e.message ?: "Napaka pri nalaganju sporočil.")
            }
        }
    }

    fun onDraftChange(v: String) { uiState = uiState.copy(draft = v) }

    fun send() {
        val id = matchId ?: return
        val body = uiState.draft.trim()
        if (body.isEmpty()) return
        uiState = uiState.copy(draft = "")
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(id, body)
                load()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }
}
