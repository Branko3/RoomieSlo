package com.roomieslo.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.roomieslo.app.domain.model.DeliveryStatus
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.roomieslo.app.domain.model.MatchStatus

@Composable
fun ChatListScreen(
    navController: NavHostController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Ujemanja in klepeti",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp)
        )
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.rows.isEmpty() -> Text(
                state.errorMessage ?: "Nimaš še ujemanj. Pošlji zahtevo iz oglasa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.rows, key = { it.match.id }) { row ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (row.match.status == MatchStatus.ACCEPTED) {
                                navController.navigate("chat/${row.match.id}")
                            }
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(row.peerName, fontWeight = FontWeight.Medium)
                            Text(
                                when (row.match.status) {
                                    MatchStatus.ACCEPTED -> "Sprejeto — odpri klepet"
                                    MatchStatus.PENDING -> if (row.iAmRecipient) "Nova zahteva za ujemanje" else "Čaka na odgovor"
                                    MatchStatus.REJECTED -> "Zavrnjeno"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (row.iAmRecipient) {
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.accept(row.match.id) }) { Text("Sprejmi") }
                                    OutlinedButton(onClick = { viewModel.reject(row.match.id) }) { Text("Zavrni") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatListScreenPreview() {
    ChatListScreen(rememberNavController())
}

@Composable
fun ChatScreen(
    navController: NavHostController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(state.messages, key = { it.id }) { message ->
                val isOwn = message.senderId == state.myUserId
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isOwn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.widthIn(max = 260.dp)
                    ) {
                        Text(
                            message.body,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (isOwn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Stanje dostave zanima le posiljatelja, zato ga prikazemo samo pri svojih sporocilih.
                    if (isOwn) {
                        Text(
                            besediloStanja(message.deliveryStatus),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = viewModel::onDraftChange,
                placeholder = { Text("Napiši sporočilo...") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.send() }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Pošlji")
            }
        }
    }
}

/** Besedilo stanja dostave, kot ga vidi posiljatelj. */
private fun besediloStanja(stanje: DeliveryStatus): String = when (stanje) {
    DeliveryStatus.SENT -> "Poslano"
    DeliveryStatus.DELIVERED -> "Dostavljeno"
    DeliveryStatus.READ -> "Prebrano"
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    ChatScreen(rememberNavController())
}
