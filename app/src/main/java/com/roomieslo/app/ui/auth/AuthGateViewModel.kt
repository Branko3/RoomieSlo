package com.roomieslo.app.ui.auth

import androidx.lifecycle.ViewModel
import com.roomieslo.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Ob zagonu aplikacije odloca o zacetnem cilju: ce obstaja shranjena seja, uporabnika
 * usmerimo naravnost na oglase, sicer na prijavo. Dokler je seja v nalaganju
 * (SessionStatus.Initializing), prikazemo nalagalni zaslon.
 */
@HiltViewModel
class AuthGateViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
}
