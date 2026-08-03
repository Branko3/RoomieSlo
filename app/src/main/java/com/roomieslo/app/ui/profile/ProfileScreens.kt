package com.roomieslo.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.roomieslo.app.domain.model.LikertOdgovor
import com.roomieslo.app.domain.model.Profile
import com.roomieslo.app.domain.model.Vprasalnik
import com.roomieslo.app.ui.common.SampleData
import com.roomieslo.app.ui.navigation.Destinations

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    // Osvezi profil ob vsakem vstopu na zaslon, da se po vrnitvi iz vprasalnika
    // posodobi stevilo izpolnjenih trditev.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) {
            navController.navigate(Destinations.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    when {
        // Vrtavko pokazemo samo pri prvem nalaganju; ob osvezitvi ostane prikazan profil,
        // da zaslon ob vsaki vrnitvi ne utripne.
        state.isLoading && state.profile == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.profile == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.errorMessage ?: "Profila ni bilo mogoce naloziti.")
            }
        }
        else -> {
            ProfileContent(
                profile = state.profile,
                questionCount = Vprasalnik.trditve.size,
                onAvailabilityChange = viewModel::setAvailability,
                onNameSave = viewModel::updateDisplayName,
                onEditQuestionnaire = { navController.navigate(Destinations.LIFESTYLE_QUESTIONNAIRE) },
                onOpenAdmin = { navController.navigate(Destinations.ADMIN_DASHBOARD) },
                onSignOut = viewModel::signOut
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: Profile,
    questionCount: Int,
    onAvailabilityChange: (Boolean) -> Unit,
    onNameSave: (String) -> Unit,
    onEditQuestionnaire: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(0.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(profile.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.academicStatusVerified) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.height(0.dp))
                        Text(" Preverjen student", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Iscem sostanovalca / sobo", fontWeight = FontWeight.Medium)
                    Text(
                        "Ko je izklopljeno, se tvoj profil ne prikazuje med priporocenimi zadetki.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = profile.isAvailable, onCheckedChange = onAvailabilityChange)
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Vprasalnik o zivljenjskem slogu", fontWeight = FontWeight.Medium)
                // Stejemo samo trditve z utezjo: odgovor "Ne zelim odgovoriti" je zapisan
                // v bazi, a pri izracunu zdruzljivosti ne steje, zato tudi tu ni izpolnjen.
                val izpolnjenih = profile.lifestyleAnswers.count { it.weight > 0f }
                Text(
                    "$izpolnjenih od $questionCount trditev izpolnjenih",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onEditQuestionnaire) {
                    Text(if (izpolnjenih == 0) "Izpolni vprasalnik" else "Uredi odgovore")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showEditDialog = true }) { Text("Uredi ime") }
            TextButton(onClick = onSignOut) {
                Text("Odjava", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onOpenAdmin) { Text("Administratorska plosca") }
    }

    if (showEditDialog) {
        var editedName by remember { mutableStateOf(profile.displayName) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Uredi ime") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Prikazano ime") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onNameSave(editedName)
                    showEditDialog = false
                }) { Text("Shrani") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Preklici") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileContent(
        profile = SampleData.profile,
        questionCount = Vprasalnik.trditve.size,
        onAvailabilityChange = {},
        onNameSave = {},
        onEditQuestionnaire = {},
        onOpenAdmin = {},
        onSignOut = {}
    )
}

@Composable
fun LifestyleQuestionnaireScreen(
    navController: NavHostController,
    viewModel: QuestionnaireViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isSaved) {
        if (!state.isSaved) return@LaunchedEffect
        // Vprasalnik je dosegljiv iz uvajanja (po preverjanju statusa) in iz profila.
        // V prvem primeru je to zadnji korak in nadaljujemo na seznam oglasov, v drugem
        // se vrnemo tja, od koder je uporabnik prisel.
        val izProfila = navController.previousBackStackEntry?.destination?.route == Destinations.PROFILE
        if (izProfila) {
            navController.popBackStack()
        } else {
            navController.navigate(Destinations.LISTINGS) {
                popUpTo(Destinations.LIFESTYLE_QUESTIONNAIRE) { inclusive = true }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 8.dp, top = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Vprasalnik o zivljenjskem slogu",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Izpolnjenih ${state.steviloIzrecenih} od ${viewModel.trditve.size} trditev",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Preskok je dosegljiv pri vsaki trditvi, ne sele na dnu seznama. Shrani enako
            // kot glavni gumb, zato preostale trditve dobijo "Ne zelim odgovoriti".
            TextButton(onClick = viewModel::shrani, enabled = !state.isSaving) { Text("Preskoci") }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(viewModel.trditve, key = { it.id }) { trditev ->
                LikertovaTrditev(
                    besedilo = trditev.besedilo,
                    izbran = state.odgovori[trditev.id],
                    omogoceno = !state.isSaving,
                    naIzbiro = { viewModel.izberi(trditev.id, it) }
                )
            }
            item {
                if (state.errorMessage != null) {
                    Text(
                        state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = viewModel::shrani,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (state.isSaving) "Shranjujem..." else "Shrani in nadaljuj") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Ena trditev z Likertovo lestvico (F04).
 *
 * Stopnje so izbirni gumbi in ne drsnik: uporabnik izbira med opisanimi moznostmi,
 * zato ni treba ugibati, kaj pomeni polozaj rocice. Zadnja moznost je "Ne zelim
 * odgovoriti", ki se v bazo zapise z utezjo 0.
 */
@Composable
private fun LikertovaTrditev(
    besedilo: String,
    izbran: LikertOdgovor?,
    omogoceno: Boolean,
    naIzbiro: (LikertOdgovor) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        Text(besedilo, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        LikertOdgovor.entries.forEach { odgovor ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = izbran == odgovor,
                        onClick = { naIzbiro(odgovor) },
                        enabled = omogoceno,
                        role = Role.RadioButton
                    )
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = izbran == odgovor, onClick = null, enabled = omogoceno)
                Text(
                    odgovor.besedilo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (odgovor.jeIzrecen) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
