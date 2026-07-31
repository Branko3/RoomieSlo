package com.roomieslo.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.roomieslo.app.data.remote.dto.ReportDto

@Composable
fun AdminDashboardScreen(
    navController: NavHostController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Administratorska plosca",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp)
        )
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            !state.isAdmin -> Text(
                "Nimas administratorskih pravic.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            state.reports.isEmpty() -> Text(
                state.errorMessage ?: "Ni prijav.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.reports, key = ReportDto::id) { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(report.reason, fontWeight = FontWeight.Medium)
                                if (report.description.isNotBlank()) {
                                    Text(report.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            val resolved = report.status != "open"
                            AssistChip(
                                onClick = { viewModel.toggleStatus(report) },
                                label = { Text(if (resolved) "Reseno" else "Odprto") },
                                colors = if (!resolved) AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ) else AssistChipDefaults.assistChipColors()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardScreenPreview() {
    AdminDashboardScreen(rememberNavController())
}
