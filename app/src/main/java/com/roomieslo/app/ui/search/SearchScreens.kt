package com.roomieslo.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.roomieslo.app.domain.model.Listing
import com.roomieslo.app.domain.model.Profile
import com.roomieslo.app.ui.common.NaloziObKoncu
import com.roomieslo.app.ui.common.PodatekStolpec
import com.roomieslo.app.ui.common.VrstaZnack
import com.roomieslo.app.ui.common.Znacka
import com.roomieslo.app.ui.common.ZnackaPreverjen
import com.roomieslo.app.ui.common.datumVselitve
import com.roomieslo.app.ui.common.jeNov
import com.roomieslo.app.ui.common.podrobnostiOglasa
import com.roomieslo.app.ui.navigation.Destinations

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Iskanje sobe", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = state.location,
            onValueChange = viewModel::onLocationChange,
            label = { Text("Lokacija") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        val zgornjaMeja = if (state.maxPrice >= SearchViewModel.NAJVECJA_CENA) "brez meje"
            else "${state.maxPrice.toInt()} EUR"
        Text("Proračun: ${state.minPrice.toInt()} - $zgornjaMeja", fontWeight = FontWeight.Medium)
        RangeSlider(
            value = state.minPrice..state.maxPrice,
            onValueChange = viewModel::onPriceRangeChange,
            valueRange = 100f..SearchViewModel.NAJVECJA_CENA
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.search() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (state.isLoading) "Iščem..." else "Poišči") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { navController.navigate(Destinations.RECOMMENDED_PROFILES) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Priporočeni sostanovalci (po združljivosti)") }

        Spacer(Modifier.height(16.dp))
        if (state.errorMessage != null) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (state.hasSearched && state.results.isEmpty() && state.errorMessage == null) {
            Text(
                "Ni zadetkov za izbrane pogoje.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val stanjeSeznama = rememberLazyListState()
        NaloziObKoncu(stanjeSeznama) { viewModel.naloziNaslednjo() }

        LazyColumn(state = stanjeSeznama, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.results, key = Listing::id) { listing ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("listing_detail/${listing.id}") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val znacke = listOf(
                            listing.roomType.takeIf { it.isNotBlank() },
                            "Novo".takeIf { jeNov(listing.createdAt) },
                            "Stroški vključeni".takeIf { listing.billsIncluded }
                        ).filterNotNull()
                        if (znacke.isNotEmpty()) {
                            VrstaZnack { znacke.forEach { Znacka(it) } }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            listing.displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            listing.displayLocation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            PodatekStolpec(
                                "Najemnina",
                                "${listing.pricePerMonth.toInt()} € / mesec",
                                Modifier.weight(1f)
                            )
                            PodatekStolpec("Na voljo od", datumVselitve(listing.availableFrom))
                        }
                        val podrobnosti = podrobnostiOglasa(listing)
                        if (podrobnosti.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                podrobnosti,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    SearchScreen(rememberNavController())
}

@Composable
fun RecommendedProfilesScreen(
    navController: NavHostController,
    viewModel: RecommendedProfilesViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Priporočeni profili",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp)
        )
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.results.isEmpty() -> Text(
                state.errorMessage ?: "Ni razpoložljivih profilov za priporočilo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.results, key = { it.first.userId }) { (profile, compatibility) ->
                    RecommendedProfileCard(
                        profile = profile,
                        compatibility = compatibility,
                        onReport = { navController.navigate("report_user/${profile.userId}") }
                    )
                }
            }
        }
    }
}

/**
 * Kartica priporocenega profila.
 *
 * Ime, starost in fakulteta so kontekst, ki ga uporabnik potrebuje, preden se odloci za
 * zahtevo; preverjeni akademski status je prikazan kot znacka in ne kot besedilo, ker je
 * to lastnost, ki je konkurencne resitve nimajo.
 */
@Composable
private fun RecommendedProfileCard(profile: Profile, compatibility: Int, onReport: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        imeInStarost(profile),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (profile.faculty.isNotBlank()) {
                        Text(
                            profile.faculty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        "$compatibility%",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            VrstaZnack {
                if (profile.academicStatusVerified) ZnackaPreverjen()
                if (profile.isAvailable) Znacka("Išče sostanovalca")
            }

            if (profile.bio.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    profile.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(onClick = onReport, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                Text("Prijavi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** "Ana K., 22" ali samo ime, ce starost ni vpisana. */
private fun imeInStarost(profile: Profile): String {
    val ime = profile.displayName.ifBlank { "Uporabnik" }
    return profile.age?.let { "$ime, $it" } ?: ime
}

@Preview(showBackground = true)
@Composable
private fun RecommendedProfilesScreenPreview() {
    RecommendedProfilesScreen(rememberNavController())
}
