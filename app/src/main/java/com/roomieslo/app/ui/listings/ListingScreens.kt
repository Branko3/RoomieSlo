package com.roomieslo.app.ui.listings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.roomieslo.app.domain.model.Listing
import com.roomieslo.app.ui.common.NaloziObKoncu
import com.roomieslo.app.ui.common.PodatekStolpec
import com.roomieslo.app.ui.common.VrstaZnack
import com.roomieslo.app.ui.common.Znacka
import com.roomieslo.app.ui.common.datumVselitve
import com.roomieslo.app.ui.common.jeNov
import com.roomieslo.app.ui.common.podrobnostiOglasa
import com.roomieslo.app.ui.common.zeZiviV

@Composable
fun ListingListScreen(
    navController: NavHostController,
    viewModel: ListingListViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    // Osvezi seznam ob vsakem vstopu na zaslon (npr. po ustvarjanju novega oglasa).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_listing") }) {
                Icon(Icons.Filled.Add, contentDescription = "Nov oglas")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "Oglasi za sobe",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.listings.isEmpty() -> Text(
                    state.errorMessage ?: "Ni oglasov. Dodaj prvega z gumbom +.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                else -> {
                    val stanjeSeznama = rememberLazyListState()
                    NaloziObKoncu(stanjeSeznama) { viewModel.naloziNaslednjo() }

                    // Napaka ob polnem predpomnilniku ni usodna, a mora biti vidna --
                    // sicer uporabnik ne ve, da gleda shranjene podatke.
                    state.errorMessage?.let { sporocilo ->
                        Text(
                            sporocilo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }

                    LazyColumn(
                        state = stanjeSeznama,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.listings, key = Listing::id) { listing ->
                            ListingCard(listing = listing, onClick = { navController.navigate("listing_detail/${listing.id}") })
                        }
                        if (state.nalagaNaslednjo) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartica oglasa v seznamu.
 *
 * Zgradba sledi ureditvi, ki jo uporabljajo primerljive resitve: najprej znacke s stanjem,
 * nato naslov in lokacija, nato cena in datum vselitve v dveh stolpcih, na dnu pa drobne
 * podrobnosti. Prazna polja se izpustijo, zato je kartica pri skopo izpolnjenem oglasu
 * krajsa, nikoli pa ne prikaze prazne vrstice.
 */
@Composable
private fun ListingCard(listing: Listing, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val znacke = listOf(
                listing.roomType.takeIf { it.isNotBlank() },
                "Novo".takeIf { jeNov(listing.createdAt) },
                "Opremljeno".takeIf { listing.furnished },
                "Stroški vključeni".takeIf { listing.billsIncluded }
            ).filterNotNull()
            if (znacke.isNotEmpty()) {
                VrstaZnack { znacke.forEach { Znacka(it) } }
                Spacer(Modifier.height(10.dp))
            }

            Text(
                listing.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    listing.displayLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                PodatekStolpec(
                    oznaka = "Najemnina",
                    vrednost = "${listing.pricePerMonth.toInt()} € / mesec",
                    modifier = Modifier.weight(1f)
                )
                PodatekStolpec(
                    oznaka = "Na voljo od",
                    vrednost = datumVselitve(listing.availableFrom)
                )
            }

            val podrobnosti = podrobnostiOglasa(listing)
            if (podrobnosti.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    podrobnosti,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ListingDetailScreen(
    navController: NavHostController,
    viewModel: ListingDetailViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.listing == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage ?: "Oglasa ni bilo mogoče naložiti.")
        }
        else -> {
            val listing = state.listing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        listing.displayTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Priljubljene",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        listing.displayLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "${listing.pricePerMonth.toInt()} EUR / mesec",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(if (listing.isFilled) "Zapolnjeno" else "Na voljo") },
                    colors = if (listing.isFilled) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) else AssistChipDefaults.assistChipColors()
                )

                // Lastnosti nastanitve. Vsaka znacka je odvisna od svojega polja, zato
                // oglas brez izpolnjenih podatkov tu preprosto ne prikaze nicesar.
                val lastnosti = listOf(
                    listing.roomType.takeIf { it.isNotBlank() },
                    "Opremljeno".takeIf { listing.furnished },
                    "Stroški vključeni".takeIf { listing.billsIncluded }
                ).filterNotNull()
                if (lastnosti.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    VrstaZnack { lastnosti.forEach { Znacka(it) } }
                }

                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    PodatekStolpec("Na voljo od", datumVselitve(listing.availableFrom), Modifier.weight(1f))
                    listing.sizeSqm?.let {
                        PodatekStolpec("Velikost", "$it m²", Modifier.weight(1f))
                    }
                    listing.deposit?.let {
                        PodatekStolpec("Varščina", "${it.toInt()} €", Modifier.weight(1f))
                    }
                }
                if (listing.flatmatesCount > 0) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        zeZiviV(listing.flatmatesCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(listing.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))

                if (!state.isOwner) {
                    Button(
                        onClick = { viewModel.sendMatchRequest() },
                        enabled = !state.isRequestSent && !listing.isFilled,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (state.isRequestSent) "Zahteva poslana" else "Pošlji zahtevo za ujemanje") }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Po sprejemu zahteve s strani lastnika oglasa se odpre klepet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.isOwner) {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Text("Za lastnika oglasa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { navController.navigate("create_listing?listingId=${listing.id}") }) {
                            Text("Uredi oglas")
                        }
                        OutlinedButton(onClick = { viewModel.toggleFilled() }) {
                            Text(if (listing.isFilled) "Označi kot na voljo" else "Označi kot zapolnjeno")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("Izbriši oglas", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Izbriši oglas?") },
                    text = { Text("Oglasa ne bo več mogoče obnoviti.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            viewModel.delete()
                        }) { Text("Izbriši", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Prekliči") }
                    }
                )
            }
        }
    }
}

@Composable
fun CreateListingScreen(
    navController: NavHostController,
    viewModel: CreateListingViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            if (viewModel.isEditing) "Uredi oglas" else "Nov oglas za sobo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Naslov oglasa") },
            supportingText = { Text("Npr. Svetla soba blizu FRI") },
            singleLine = true,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.location,
            onValueChange = viewModel::onLocationChange,
            label = { Text("Lokacija") },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.price,
            onValueChange = viewModel::onPriceChange,
            label = { Text("Cena na mesec (EUR)") },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        Text("Vrsta nastanitve", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        // Zaprt nabor namesto prostega vnosa: enake vrednosti so pogoj, da je kasneje
        // mogoce filtrirati po vrsti nastanitve.
        VrstaZnack {
            VRSTE_NASTANITVE.forEach { vrsta ->
                FilterChip(
                    selected = state.roomType == vrsta,
                    onClick = { viewModel.onRoomTypeChange(if (state.roomType == vrsta) "" else vrsta) },
                    label = { Text(vrsta) },
                    enabled = !state.isSaving
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Četrt", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        VrstaZnack {
            CETRTI.forEach { cetrt ->
                FilterChip(
                    selected = state.district == cetrt,
                    onClick = { viewModel.onDistrictChange(if (state.district == cetrt) "" else cetrt) },
                    label = { Text(cetrt) },
                    enabled = !state.isSaving
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.availableFrom,
            onValueChange = viewModel::onAvailableFromChange,
            label = { Text("Na voljo od") },
            supportingText = { Text("LLLL-MM-DD; prazno pomeni po dogovoru") },
            singleLine = true,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.sizeSqm,
                onValueChange = viewModel::onSizeChange,
                label = { Text("Velikost (m²)") },
                singleLine = true,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.deposit,
                onValueChange = viewModel::onDepositChange,
                label = { Text("Varščina (EUR)") },
                singleLine = true,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.flatmates,
            onValueChange = viewModel::onFlatmatesChange,
            label = { Text("Število sostanovalcev") },
            singleLine = true,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stroški vključeni v ceno", modifier = Modifier.weight(1f))
            Switch(
                checked = state.billsIncluded,
                onCheckedChange = viewModel::onBillsChange,
                enabled = !state.isSaving
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Opremljeno", modifier = Modifier.weight(1f))
            Switch(
                checked = state.furnished,
                onCheckedChange = viewModel::onFurnishedChange,
                enabled = !state.isSaving
            )
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Opis") },
            minLines = 4,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.save() },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (state.isSaving) "Shranjujem..." else "Objavi oglas") }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListingListScreenPreview() {
    ListingListScreen(rememberNavController())
}
