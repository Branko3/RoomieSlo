package com.roomieslo.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.roomieslo.app.domain.model.Listing
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Majhna barvna oznaka (znacka) nad naslovom kartice.
 *
 * Znacke nosijo podatek, ki bi sicer zavzel celo poved -- vrsto nastanitve, opremljenost,
 * vkljucene stroske. Namenjene so branju z enim pogledom, zato so kratke in brez ikon.
 */
@Composable
fun Znacka(
    besedilo: String,
    barvaOzadja: Color = MaterialTheme.colorScheme.secondaryContainer,
    barvaBesedila: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(shape = RoundedCornerShape(50), color = barvaOzadja) {
        Text(
            besedilo,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = barvaBesedila
        )
    }
}

/**
 * Znacka preverjenega akademskega statusa (F02).
 *
 * Prikazana je z ikono in v barvi primarnega kontejnerja, ker je edina lastnost, ki je
 * konkurencne resitve nimajo -- prikaz z navadnim besedilom je to izenacil z ostalim opisom.
 */
@Composable
fun ZnackaPreverjen(besedilo: String = "Preverjen študent") {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                besedilo,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/** Oznaka in vrednost v dveh vrsticah -- za stolpce s podatki (cena, datum vselitve ...). */
@Composable
fun PodatekStolpec(oznaka: String, vrednost: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            oznaka,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(vrednost, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

/** Vrstica znack, ki se prelomi, ce jih je vec, kot jih gre v sirino. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VrstaZnack(modifier: Modifier = Modifier, vsebina: @Composable () -> Unit) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) { vsebina() }
}

/**
 * Ali je oglas nastal v zadnjih [dni] dneh.
 *
 * `createdAt` je ISO 8601 z odmikom, kot ga vrne baza. Ce ga ni mogoce razcleniti
 * (prazen niz pri predogledu, star zapis), oglas ne velja za nov -- raje brez znacke
 * kot z napacno.
 */
fun jeNov(createdAt: String, dni: Long = 7): Boolean = runCatching {
    OffsetDateTime.parse(createdAt).toLocalDate().isAfter(LocalDate.now().minusDays(dni))
}.getOrDefault(false)

/**
 * Datum vselitve za prikaz: "takoj" za pretekle datume, sicer "1. sep".
 * Null pomeni, da datum ni vpisan -- takrat prikazemo "po dogovoru".
 */
fun datumVselitve(iso: String?): String {
    if (iso.isNullOrBlank()) return "po dogovoru"
    val datum = runCatching { LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE) }
        .getOrNull() ?: return "po dogovoru"
    if (!datum.isAfter(LocalDate.now())) return "takoj"
    val mesec = datum.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("sl"))
    return "${datum.dayOfMonth}. $mesec"
}

/**
 * Drobne podrobnosti oglasa v eni vrstici, npr. "32 m² · 2 sostanovalca · varščina 380 €".
 * Polja brez podatka se izpustijo, da ne nastane vrstica s praznimi vrednostmi.
 */
fun podrobnostiOglasa(listing: Listing): String = buildList {
    listing.sizeSqm?.let { add("$it m²") }
    if (listing.flatmatesCount > 0) add(sostanovalci(listing.flatmatesCount))
    listing.deposit?.let { add("varščina ${it.toInt()} €") }
}.joinToString(" · ")

/**
 * Stevilo sostanovalcev v pravilni slovnicni obliki.
 *
 * Slovenscina ima poleg ednine in mnozine se dvojino, ob stevilih od pet naprej pa se
 * rodilnik mnozine. Enotna oblika ("2 sostanovalcev") bi bila slovnicno napacna.
 */
fun sostanovalci(n: Int): String = when {
    n % 100 == 1 -> "$n sostanovalec"
    n % 100 == 2 -> "$n sostanovalca"
    n % 100 == 3 || n % 100 == 4 -> "$n sostanovalci"
    else -> "$n sostanovalcev"
}

/** Poved o zasedenosti stanovanja, s pravilnim glagolom za ednino, dvojino in mnozino. */
fun zeZiviV(n: Int): String = when {
    n % 100 == 1 -> "V stanovanju že živi ${sostanovalci(n)}."
    n % 100 == 2 -> "V stanovanju že živita ${sostanovalci(n)}."
    n % 100 == 3 || n % 100 == 4 -> "V stanovanju že živijo ${sostanovalci(n)}."
    else -> "V stanovanju že živi ${sostanovalci(n)}."
}
