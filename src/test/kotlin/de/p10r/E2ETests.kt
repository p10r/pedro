package de.p10r

import de.p10r.fixtures.App
import de.p10r.fixtures.TelegramUser
import de.p10r.ra.RAArtist
import de.p10r.ra.RAArtistResponse
import de.p10r.ra.RASlug
import de.p10r.scenarios.TelegramUserScenario
import de.p10r.telegram.TelegramConfig
import de.p10r.telegram.TelegramMessage
import org.http4k.core.Uri

class E2ETests : TelegramUserScenario {
  val userId = UserId(1)
  val system = App(
    telegramChats = mapOf(
      userId to mutableListOf<TelegramMessage>(),
    ),
    raArtists = mapOf(
      RASlug("boysnoize") to RAArtistResponse(
        RAArtistResponse.RAData(
          RAArtist("943", "Boys Noize")
        )
      )
    )
  )

  override val telegramUser = TelegramUser(
    userId = userId,
    secret = TelegramConfig.IncomingTelegramRequestSecret("telegram-request-secret"),
    baseUri = Uri.of("http://pedro"),
    http = system
  )
}
