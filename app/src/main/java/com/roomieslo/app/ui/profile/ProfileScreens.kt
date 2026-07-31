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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.roomieslo.app.domain.model.Profile
import com.roomieslo.app.ui.common.SampleData
import com.roomieslo.app.ui.navigation.Destinations

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) {
            navController.navigate(Destinations.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.profile == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.errorMessage ?: "Profila ni bilo mogoče naložiti.")
            }
        }
        else -> {
            ProfileContent(
                profile = state.profile,
                questionCount = SampleData.lifestyleQuestions.size,
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
                        Text(" Preverjen študent", style = MaterialTheme.typography.bodySmall)
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
                    Text("Iščem sostanovalca / sobo", fontWeight = FontWeight.Medium)
                    Text(
                        "Ko je izklopljeno, se tvoj profil ne prikazuje med priporočenimi zadetki.",
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
                Text("Vprašalnik o življenjskem slogu", fontWeight = FontWeight.Medium)
                Text(
                    "${profile.lifestyleAnswers.size} od $questionCount vprašanj izpolnjenih",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onEditQuestionnaire) { Text("Uredi odgovore") }
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
        TextButton(onClick = onOpenAdmin) { Text("Administratorska plošča") }
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
                TextButton(onClick = { showEditDialog = false }) { Text("Prekliči") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileContent(
        profile = SampleData.profile,
        questionCount = SampleData.lifestyleQuestions.size,
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
        if (state.isSaved) {
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
        Text(
            "Vprašalnik o življenjskem slogu",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(viewModel.questions) { (question, id) ->
                val value = state.values[id] ?: 0.5f
                Column {
                    Text(question, fontWeight = FontWeight.Medium)
                    Slider(
                        value = value,
                        onValueChange = { viewModel.setValue(id, it) },
                        enabled = !state.isSaving
                    )
                }
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
                    onClick = { viewModel.save() },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (state.isSaving) "Shranjujem..." else "Shrani in nadaljuj") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
