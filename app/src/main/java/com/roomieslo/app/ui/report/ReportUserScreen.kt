package com.roomieslo.app.ui.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

private val reasons = listOf("Neprimerna vsebina", "Lažen profil ali oglas", "Nadlegovanje", "Drugo")

@Composable
fun ReportUserScreen(
    navController: NavHostController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Prijavi uporabnika", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.selectableGroup()) {
            reasons.forEach { reason ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.reason == reason,
                        onClick = { viewModel.onReasonChange(reason) }
                    )
                    Text(reason)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Dodatni opis (neobvezno)") },
            minLines = 3,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.submit() },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (state.isSubmitting) "Pošiljam..." else "Pošlji prijavo") }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportUserScreenPreview() {
    ReportUserScreen(rememberNavController())
}
