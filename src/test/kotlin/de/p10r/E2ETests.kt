package de.p10r

import de.p10r.adapters.driven.ra.RAArtist
import de.p10r.adapters.driven.ra.RAArtistResponse
import de.p10r.adapters.driven.ra.RASlug
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driven.telegram.TelegramMessage
import de.p10r.domain.UserId
import de.p10r.fixtures.HttpTelegramUser
import de.p10r.fixtures.TestE2EApp
import de.p10r.scenarios.TelegramUserScenarios
import org.http4k.core.Uri

class E2ETests : TelegramUserScenarios {
  val sarahsId = UserId(1)
  val joesId = UserId(2)
  val raArtists = mapOf(
    RASlug("boysnoize") to RAArtistResponse(
      RAArtistResponse.RAData(
        RAArtist("943", "Boys Noize")
      )
    ),
    RASlug("sabura") to RAArtistResponse(
      RAArtistResponse.RAData(
        RAArtist("2", "Sabura")
      )
    ),
    RASlug("justice") to RAArtistResponse(
      RAArtistResponse.RAData(
        RAArtist("3", "Justice")
      )
    )
  )

  val system = TestE2EApp(
    telegramChats = mapOf(
      sarahsId to mutableListOf<TelegramMessage>(),
      joesId to mutableListOf<TelegramMessage>(),
    ),
    raArtists = raArtists
  )

  override val sarah = HttpTelegramUser(
    userId = sarahsId,
    secret = TelegramConfig.IncomingTelegramRequestSecret("telegram-request-secret"),
    baseUri = Uri.of("http://pedro"),
    http = system
  )

  override val joe = HttpTelegramUser(
    userId = joesId,
    secret = TelegramConfig.IncomingTelegramRequestSecret("telegram-request-secret"),
    baseUri = Uri.of("http://pedro"),
    http = system
  )

}
