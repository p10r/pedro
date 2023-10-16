package de.p10r.fixtures

import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driving.ArtistResponse
import de.p10r.adapters.driving.artists
import de.p10r.adapters.driving.telegramReq
import de.p10r.domain.UserCommandHub
import de.p10r.domain.models.ArtistName
import de.p10r.domain.models.UserCommand
import de.p10r.domain.models.UserCommandResult
import de.p10r.domain.models.UserId
import de.p10r.incomingTelegramReqOf
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ClientFilters
import strikt.api.expectThat
import strikt.assertions.isEqualTo

interface TelegramUser {
  fun followArtist(url: String)

  fun listArtists(): List<ArtistResponse>
}

class HttpTelegramUser(
  private val userId: UserId,
  private val secret: TelegramConfig.IncomingTelegramRequestSecret,
  baseUri: Uri,
  http: HttpHandler
) : TelegramUser {
  val http = ClientFilters.ResetRequestTracing()
    .then(ClientFilters.SetHostFrom(baseUri))
    .then(http)

  override fun followArtist(url: String) {
    val res = Request(POST, "/telegram")
      .header("X-Telegram-Bot-Api-Secret-Token", secret.value)
      .with(telegramReq of incomingTelegramReqOf(userId, "/add $url"))
      .let(http)

    expectThat(res.status).isEqualTo(Status.OK)
  }

  override fun listArtists(): List<ArtistResponse> {
    val res = Request(POST, "/telegram")
      .header("X-Telegram-Bot-Api-Secret-Token", secret.value)
      .with(telegramReq of incomingTelegramReqOf(userId, "/list"))
      .let(http)
    expectThat(res.status).isEqualTo(Status.OK)
    return artists(res)
  }
}

class DomainTelegramUser(val hub: UserCommandHub, val userId: UserId) : TelegramUser {
  override fun followArtist(url: String) {
    hub.process(UserCommand.FollowArtist(userId, ArtistName.of(url)))
  }

  override fun listArtists(): List<ArtistResponse> {
    return (hub.process(UserCommand.ListArtists(userId)) as UserCommandResult.Artists).artists
      .map { ArtistResponse(it.name) }
  }

}
