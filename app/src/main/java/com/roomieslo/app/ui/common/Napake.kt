package com.roomieslo.app.ui.common

import java.io.IOException

/**
 * Pretvori izjemo v sporocilo, primerno za uporabnika.
 *
 * Surova sporocila iz Ktorja vsebujejo cel URL zahtevka in podrobnosti HTTP odgovora
 * (npr. "HTTP request to https://... failed with message: Unable to resolve host").
 * Uporabniku ne povedo nicesar, hkrati pa razkrivajo zgradbo zaledja.
 *
 * Opomba: vzorec `e.message ?: "privzeto"` ne deluje, ker izjeme iz omreznega sloja
 * skoraj nikoli nimajo praznega `message`, zato se privzeto sporocilo ni nikoli prikazalo.
 */
fun Throwable.uporabnisko(privzeto: String): String = when (this) {
    is IOException -> "Ni povezave. Preveri internetno povezavo."
    else -> privzeto
}
