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
        ProfileMatch(profile.copy(userId = "u2", displayName = "Iva K."), 92),
        ProfileMatch(profile.copy(userId = "u3", displayName = "Marko P."), 84),
        ProfileMatch(profile.copy(userId = "u4", displayName = "Sara D."), 77),
        ProfileMatch(profile.copy(userId = "u5", displayName = "Luka T."), 63)
    )

    val listings = listOf(
        Listing("l1", "u2", "Ljubljana - Bezigrad", 320.0, "Svetla soba v skupnem stanovanju blizu FRI, na voljo od septembra.", false, "2026-07-24T09:15:00Z"),
        Listing("l2", "u3", "Ljubljana - Center", 450.0, "Soba v prenovljenem stanovanju, 5 min od Presernovega trga.", false, "2026-07-22T14:40:00Z"),
        Listing("l3", "u4", "Ljubljana - siska", 280.0, "Skupno bivanje s tremi studenti, kuhinja in dnevna soba v skupni rabi.", false, "2026-07-19T08:05:00Z"),
        Listing("l4", "u5", "Ljubljana - Vic", 350.0, "Soba blizu studentskih naselij, dobra povezava z avtobusom.", true, "2026-07-15T17:30:00Z")
    )

    val chats = listOf(
        Match("m1", "u1", "u2", MatchStatus.ACCEPTED, version = 3) to "Se vidiva jutri ob 17h za ogled?",
        Match("m2", "u1", "u3", MatchStatus.ACCEPTED, version = 1) to "Super, hvala za informacije!",
        Match("m3", "u1", "u4", MatchStatus.PENDING, version = 0) to "Zahteva za ujemanje poslana."
    )

    val messages = listOf(
        Message("msg1", "m1", "u2", "Pozdravljen, me zanima, ali je soba se na voljo?", DeliveryStatus.READ),
        Message("msg2", "m1", "u1", "zivjo! Ja, je se prosta. Kdaj bi si jo zelel ogledati?", DeliveryStatus.READ),
        Message("msg3", "m1", "u2", "Se vidiva jutri ob 17h za ogled?", DeliveryStatus.DELIVERED)
    )
}
