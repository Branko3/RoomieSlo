package com.roomieslo.app.ui.common

import com.roomieslo.app.domain.model.DeliveryStatus
import com.roomieslo.app.domain.model.LifestyleAnswer
import com.roomieslo.app.domain.model.Listing
import com.roomieslo.app.domain.model.Match
import com.roomieslo.app.domain.model.MatchStatus
import com.roomieslo.app.domain.model.Message
import com.roomieslo.app.domain.model.Profile

/** Vzorčni podatki za predogled zaslonov (Compose Preview) in vizualni razvoj vmesnika. */
object SampleData {

    val lifestyleQuestions = listOf(
        "Jutranji ali nočni tip?" to "q_wake",
        "Pogostost gostov v stanovanju" to "q_guests",
        "Odnos do čistoče skupnih prostorov" to "q_cleanliness",
        "Kajenje v stanovanju" to "q_smoking",
        "Glasnost (glasba, klici)" to "q_noise"
    )

    val profile = Profile(
        userId = "u1",
        displayName = "Amar H.",
        academicStatusVerified = true,
        isAvailable = true,
        lifestyleAnswers = listOf(
            LifestyleAnswer("q_wake", 0.3f, 1f),
            LifestyleAnswer("q_guests", 0.5f, 0.7f),
            LifestyleAnswer("q_cleanliness", 0.8f, 1f)
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
        Listing("l1", "u2", "Ljubljana - Bežigrad", 320.0, "Svetla soba v skupnem stanovanju blizu FRI, na voljo od septembra.", false),
        Listing("l2", "u3", "Ljubljana - Center", 450.0, "Soba v prenovljenem stanovanju, 5 min od Prešernovega trga.", false),
        Listing("l3", "u4", "Ljubljana - Šiška", 280.0, "Skupno bivanje s tremi študenti, kuhinja in dnevna soba v skupni rabi.", false),
        Listing("l4", "u5", "Ljubljana - Vič", 350.0, "Soba blizu študentskih naselij, dobra povezava z avtobusom.", true)
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
