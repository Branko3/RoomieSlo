package com.roomieslo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Barvna shema RoomieSlo.
 *
 * Shema je izpisana v celoti in ne le s tremi ali stirimi vrednostmi. Material 3 namrec
 * manjkajocih vlog ne izpelje iz podane primarne barve, ampak zanje uporabi svojo privzeto
 * (vijolicno) paleto. Ker gumb FloatingActionButton privzeto uporabi `primaryContainer`,
 * izbrani zavihek v navigaciji `secondaryContainer`, kartica oglasa pa `surfaceVariant`,
 * so bili prav ti deli vmesnika vijolicni, cetudi je bila primarna barva zelena.
 *
 * Izhodisci sta zelena 0xFF2F6F5E in jantarna 0xFFE7B75F; preostale vloge so njuni
 * svetlejsi oziroma temnejsi odtenki, izbrani tako, da razmerje kontrasta med parom
 * `X` in `onX` ostane nad 4,5 : 1 (WCAG 2.1, merilo 1.4.3).
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6F5E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB4EBD8),
    onPrimaryContainer = Color(0xFF00201A),

    secondary = Color(0xFF6E5B2E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7E4BC),
    onSecondaryContainer = Color(0xFF241A00),

    tertiary = Color(0xFF3C6472),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC0E9FA),
    onTertiaryContainer = Color(0xFF001F28),

    background = Color(0xFFFAFAF7),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDCE5E0),
    onSurfaceVariant = Color(0xFF404944),

    outline = Color(0xFF707972),
    outlineVariant = Color(0xFFC0C9C3),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FBCA8),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF17503F),
    onPrimaryContainer = Color(0xFFB4EBD8),

    secondary = Color(0xFFE7B75F),
    onSecondary = Color(0xFF3E2E00),
    secondaryContainer = Color(0xFF574400),
    onSecondaryContainer = Color(0xFFF7E4BC),

    tertiary = Color(0xFFA5CDDE),
    onTertiary = Color(0xFF06363F),
    tertiaryContainer = Color(0xFF234C56),
    onTertiaryContainer = Color(0xFFC0E9FA),

    background = Color(0xFF191C1B),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF191C1B),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFC0C9C3),

    outline = Color(0xFF8A938C),
    outlineVariant = Color(0xFF404944),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

/**
 * Privzeto sledimo sistemski nastavitvi. Prej je bil parameter trdno nastavljen na `false`,
 * zato je aplikacija tudi v temnem nacinu naprave prikazovala svetlo temo.
 */
@Composable
fun RoomieSloTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
