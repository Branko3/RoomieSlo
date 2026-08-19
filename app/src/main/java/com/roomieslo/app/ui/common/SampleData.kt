package com.roomieslo.app.ui.common

import com.roomieslo.app.domain.model.DeliveryStatus
import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.domain.model.Listing
import com.roomieslo.app.domain.model.Match
import com.roomieslo.app.domain.model.MatchStatus
import com.roomieslo.app.domain.model.Message
import com.roomieslo.app.domain.model.Profile

/** Vzorcni podatki za predogled zaslonov (Compose Preview) in vizualni razvoj vmesnika. */
object SampleData {

    val profile = Profile(
        userId = "u1",
        displayName = "Amar H.",
        academicStatusVerified = true,
        isAvailable = true,
        age = 22,
        faculty = "FRI, Univerza v Ljubljani",
        bio = "Študent računalništva iz Sarajeva. Med tednom se učim doma, ob vikendih sem " +
            "pogosto zunaj. Iščem mirno stanovanje blizu fakultete.",
        // Utezi ustrezajo tistim iz Vprasalnik.trditve; predogled tako kaze mogoce stanje.
        lifestyleAnswers = listOf(
            LifestyleAnswer("q_cleanliness", 0.75f, 0.1099f),
            LifestyleAnswer("q_costs", 1f, 0.1056f),
            LifestyleAnswer("q_chores", 0.75f, 0.0992f),
            LifestyleAnswer("q_noise", 0.5f, 0.0904f),
            LifestyleAnswer("q_guests", 0.5f, 0.0820f),
            LifestyleAnswer("q_wake", 0.25f, 0.0675f),
            // Utez 0: uporabnik na to trditev ni zelel odgovoriti.
            LifestyleAnswer("q_smoking", 0.5f, 0f)
        )
    )

    data class ProfileMatch(val profile: Profile, val compatibility: Int)

    val recommendedProfiles = listOf(
        ProfileMatch(
            profile.copy(
                userId = "u2", displayName = "Iva K.", age = 21, faculty = "FDV",
                bio = "Študentka komunikologije. Rada kuham, zvečer imam rada mir."
            ), 92
        ),
        ProfileMatch(
            profile.copy(
                userId = "u3", displayName = "Marko P.", age = 23, faculty = "FE",
                bio = "Tretji letnik elektrotehnike, veliko sem na faksu."
            ), 84
        ),
        ProfileMatch(
            profile.copy(
                userId = "u4", displayName = "Sara D.", age = 20, faculty = "FF",
                academicStatusVerified = false,
                bio = "Prvi letnik primerjalne književnosti, iščem cimro v centru."
            ), 77
        ),
        ProfileMatch(
            profile.copy(
                userId = "u5", displayName = "Luka T.", age = 24, faculty = "EF",
                bio = "Podiplomski študent ekonomije, delam od doma dvakrat na teden."
            ), 63
        )
    )

    /**
     * Oglasi za predogled. Namenoma so izpolnjeni tako, kot bi jih izpolnil skrben
     * uporabnik -- predogled tako kaze kartico v polni obliki, ne v najbolj skopi.
     */
    val listings = listOf(
        Listing(
            id = "l1", ownerId = "u2", location = "Ljubljana", pricePerMonth = 320.0,
            description = "Svetla soba v skupnem stanovanju, tri minute do postaje. Stanovanje " +
                "je pred kratkim prenovljeno, kuhinja in kopalnica sta skupni.",
            isFilled = false, createdAt = "2026-08-16T09:15:00Z",
            title = "Svetla soba blizu FRI", roomType = "soba", district = "Bežigrad",
            availableFrom = "2026-09-01", sizeSqm = 16, deposit = 320.0,
            billsIncluded = true, furnished = true, flatmatesCount = 2
        ),
        Listing(
            id = "l2", ownerId = "u3", location = "Ljubljana", pricePerMonth = 450.0,
            description = "Garsonjera pet minut od Prešernovega trga. Primerna za enega " +
                "študenta, vsa oprema je vključena.",
            isFilled = false, createdAt = "2026-08-11T14:40:00Z",
            title = "Garsonjera v starem mestnem jedru", roomType = "garsonjera", district = "Center",
            availableFrom = "2026-10-01", sizeSqm = 28, deposit = 450.0,
            billsIncluded = false, furnished = true, flatmatesCount = 0
        ),
        Listing(
            id = "l3", ownerId = "u4", location = "Ljubljana", pricePerMonth = 280.0,
            description = "Soba v stanovanju, kjer živijo trije študenti. Kuhinjo in dnevno " +
                "sobo si delimo, vsak ima svojo sobo.",
            isFilled = false, createdAt = "2026-07-19T08:05:00Z",
            title = "Soba v stanovanju s tremi študenti", roomType = "deljeno stanovanje",
            district = "Šiška", availableFrom = null, sizeSqm = 12,
            billsIncluded = false, furnished = false, flatmatesCount = 3
        ),
        Listing(
            id = "l4", ownerId = "u5", location = "Ljubljana", pricePerMonth = 350.0,
            description = "Soba blizu študentskih naselij, avtobusna postaja je pred vhodom.",
            isFilled = true, createdAt = "2026-07-15T17:30:00Z",
            title = "Soba blizu Rožne doline", roomType = "soba", district = "Vič",
            availableFrom = "2026-09-15", sizeSqm = 14, deposit = 350.0,
            furnished = true, flatmatesCount = 1
        )
    )

    val chats = listOf(
        Match("m1", "u1", "u2", MatchStatus.ACCEPTED, version = 3) to "Se vidiva jutri ob 17h za ogled?",
        Match("m2", "u1", "u3", MatchStatus.ACCEPTED, version = 1) to "Super, hvala za informacije!",
        Match("m3", "u1", "u4", MatchStatus.PENDING, version = 0) to "Zahteva za ujemanje poslana."
    )

    val messages = listOf(
        Message("msg1", "m1", "u2", "Pozdravljen, me zanima, ali je soba še na voljo?", DeliveryStatus.READ),
        Message("msg2", "m1", "u1", "Živjo! Ja, je še prosta. Kdaj bi si jo želel ogledati?", DeliveryStatus.READ),
        Message("msg3", "m1", "u2", "Se vidiva jutri ob 17h za ogled?", DeliveryStatus.DELIVERED)
    )
}
