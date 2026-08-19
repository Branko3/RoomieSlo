package com.roomieslo.app.domain.model

/** F03 */
data class Profile(
    val userId: String,
    val displayName: String,
    val academicStatusVerified: Boolean, // F02
    val isAvailable: Boolean,            // F05
    val lifestyleAnswers: List<LifestyleAnswer>, // F04
    /**
     * Predstavitvena polja profila. Vsa imajo privzeto vrednost, ker jih uporabnik ni
     * dolzan izpolniti in ker so bili profili ustvarjeni, preden so polja obstajala.
     * Prazen niz oziroma null pomeni "ni podatka" in se v vmesniku ne izpise.
     */
    val age: Int? = null,
    val faculty: String = "",
    val bio: String = "",
    val avatarUrl: String = ""
)

/** Odgovor na eno vprasanje vprasalnika o zivljenjskem slogu (F04). */
data class LifestyleAnswer(
    val questionId: String,
    val value: Float,  // normalizirana vrednost odgovora, npr. 0f..1f
    val weight: Float  // utez vprasanja pri izracunu zdruzljivosti (F12)
)

/** F06 */
data class Listing(
    val id: String,
    val ownerId: String,
    val location: String,
    val pricePerMonth: Double,
    val description: String,
    val isFilled: Boolean, // F09
    /**
     * Cas nastanka v zapisu ISO 8601, kot ga vrne baza (npr. 2026-07-20T10:00:00Z).
     * Oglasi so razvrsceni po tem polju, hkrati pa sluzi kot kazalec pri strancenju.
     */
    val createdAt: String,
    /**
     * Predstavitvena polja oglasa (migracija 0002). Nastopajo za `createdAt` in imajo
     * privzete vrednosti, da obstojeca mesta, ki oglas gradijo po vrstnem redu
     * argumentov, ostanejo veljavna.
     *
     * Prazen niz in null pomenita "ni podatka": vmesnik takega polja ne izpise, namesto
     * da bi prikazal prazno vrstico ali niclo.
     */
    val title: String = "",
    val roomType: String = "",
    val district: String = "",
    /** Datum vselitve (ISO 8601, npr. 2026-09-01). Null pomeni "po dogovoru". */
    val availableFrom: String? = null,
    val sizeSqm: Int? = null,
    val deposit: Double? = null,
    val billsIncluded: Boolean = false,
    val furnished: Boolean = false,
    /** Stevilo sostanovalcev v stanovanju (0 = uporabnik bi stanoval sam). */
    val flatmatesCount: Int = 0,
    /** Pripravljeno za prikaz fotografij; vmesnik ga zaenkrat se ne uporablja. */
    val photoUrl: String = ""
) {
    /** Naslov za prikaz: dokler oglasi nimajo vpisanega naslova, ga nadomesti lokacija. */
    val displayTitle: String get() = title.ifBlank { location }

    /**
     * Lokacija in cetrt v eni vrstici.
     *
     * Cetrt dodamo samo, ce je lokacija se ne vsebuje. Stari oglasi imajo lokacijo zapisano
     * kot "Ljubljana - Center", zato bi jo sicer izpisali dvakrat ("Ljubljana - Center — Center").
     */
    val displayLocation: String get() = when {
        district.isBlank() -> location
        location.contains(district, ignoreCase = true) -> location
        else -> "$location — $district"
    }
}

/** F15 */
enum class MatchStatus { PENDING, ACCEPTED, REJECTED }

/** F15: polje `version` je rezervirano za verzioniranje,
 * a se trenutno ne uporablja -- znotraj MatchRepository.acceptMatch(). */

data class Match(
    val id: String,
    val userIdA: String,
    val userIdB: String,
    val status: MatchStatus,
    val version: Int
)

/** F14 */
data class Message(
    val id: String,
    val matchId: String,
    val senderId: String,
    val body: String,
    val deliveryStatus: DeliveryStatus
)

enum class DeliveryStatus { SENT, DELIVERED, READ }
