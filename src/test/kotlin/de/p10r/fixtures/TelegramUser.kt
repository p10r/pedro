package de.p10r.fixtures

import de.p10r.UserId
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driving.ArtistResponse
import de.p10r.adapters.driving.artists
import de.p10r.adapters.driving.telegramCommand
import de.p10r.incomingTelegramReqOf
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ClientFilters
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class TelegramUser(
  private val userId: UserId,
  private val secret: TelegramConfig.IncomingTelegramRequestSecret,
  baseUri: Uri,
  http: HttpHandler
) {
  val http = ClientFilters.ResetRequestTracing()
    .then(ClientFilters.SetHostFrom(baseUri))
    .then(http)

  fun followArtist(url: String) {
    val res = Request(POST, "/telegram")
      .header("X-Telegram-Bot-Api-Secret-Token", secret.value)
      .with(telegramCommand of incomingTelegramReqOf(userId, "/add $url"))
      .let(http)

    expectThat(res.status).isEqualTo(Status.OK)
  }

  fun listArtists(): List<ArtistResponse> {
    val res = http(Request(GET, "/artists"))

    expectThat(res.status).isEqualTo(Status.OK)

    return artists(res)
  }

  fun listArtistsViaTelegram(): List<ArtistResponse> {
    val res = Request(POST, "/telegram")
      .header("X-Telegram-Bot-Api-Secret-Token", secret.value)
      .with(telegramCommand of incomingTelegramReqOf(userId, "/list"))
      .let(http)

    expectThat(res.status).isEqualTo(Status.OK)

    return artists(res)
  }
}
