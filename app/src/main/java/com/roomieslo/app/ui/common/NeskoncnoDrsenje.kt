package com.roomieslo.app.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Poklice [naloziNaslednjo], ko se uporabnik pri drsenju priblize koncu seznama.
 *
 * Naslednja stran se zacne prenasati [prag] elementov pred koncem, da uporabnik ne
 * caka na prazen seznam. Za zascito pred podvojenimi zahtevami skrbi ViewModel
 * (zastavici `nalagaNaslednjo` in `jeKonec`).
 */
@Composable
fun NaloziObKoncu(
    stanje: LazyListState,
    prag: Int = 3,
    naloziNaslednjo: () -> Unit
) {
    // derivedStateOf: seznam se ob drsenju prerise veckrat na sekundo, sprozilec pa
    // se sme spremeniti samo takrat, ko se dejansko prevesi cez prag.
    val jeBlizuKonca by remember(stanje, prag) {
        derivedStateOf {
            val zadnjiViden = stanje.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val skupaj = stanje.layoutInfo.totalItemsCount
            zadnjiViden != null && skupaj > 0 && zadnjiViden >= skupaj - 1 - prag
        }
    }

    LaunchedEffect(jeBlizuKonca) {
        if (jeBlizuKonca) naloziNaslednjo()
    }
}
