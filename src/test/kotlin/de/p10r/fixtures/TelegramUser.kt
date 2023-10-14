package de.p10r.fixtures

import de.p10r.Artist
import de.p10r.InputUrl
import de.p10r.UserId
import de.p10r.artists
import de.p10r.telegram.IncomingTelegramRequest
import de.p10r.telegram.TelegramConfig
import de.p10r.telegramCommand
import org.http4k.core.HttpHandler
import org.http4k.core.Method
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

  fun followArtist(url: InputUrl) {
    val req = Request(Method.POST, "/telegram")
      .header("X-Telegram-Bot-Api-Secret-Token", secret.value)
      .with(
        telegramCommand of IncomingTelegramRequest(
          IncomingTelegramRequest.Message(
            IncomingTelegramRequest.Message.From(userId.value),
            text = "/add ${url.value}",
            listOf(IncomingTelegramRequest.Message.Entity("bot_command"))
          )
        )
      )

    val res = http(req)

    expectThat(res.status).isEqualTo(Status.OK)
  }

  fun listArtists(): List<Artist> {
    val res = http(Request(Method.GET, "/artists"))
    return artists(res)
  }
}
