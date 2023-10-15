package de.p10r.adapters.driving

import de.p10r.UserId
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driven.telegram.TelegramMessage
import de.p10r.domain.Artist
import de.p10r.domain.InputUrl
import de.p10r.infrastructure.AppIncomingHttp
import de.p10r.infrastructure.UncaughtExceptionEvent
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.events.Events
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes

val artists = Body.auto<List<Artist>>().toLens()
val telegramCommand = Body.auto<IncomingTelegramRequest>().toLens()

fun ApiRoutes(
  followArtist: (InputUrl) -> Unit,
  listAllArtists: () -> List<Artist>,
  secret: TelegramConfig.IncomingTelegramRequestSecret,
  users: List<UserId>,
  sendMessage: (message: TelegramMessage, userId: UserId) -> Response,
  events: Events,
) = AppIncomingHttp(
  events,
  ServerFilters.CatchAll {
    events(UncaughtExceptionEvent(it))
    Response(Status.INTERNAL_SERVER_ERROR)
  }.then(
    routes(
      "/artists" bind GET to ListArtists(listAllArtists),
      "/telegram" bind POST to TelegramApi(users, secret, followArtist, sendMessage)
    )
  )
)

private fun ListArtists(
  listAllArtists: () -> List<Artist>
) = { req: Request -> Response(OK).with(artists of listAllArtists()) }

private fun TelegramApi(
  users: List<UserId>,
  secret: TelegramConfig.IncomingTelegramRequestSecret,
  followArtist: (InputUrl) -> Unit,
  sendMessage: (message: TelegramMessage, userId: UserId) -> Response
) = TelegramSecurityFilter(users, secret).then { req ->
  val payload = telegramCommand(req)

  when {
    payload.message.entities.none { it.type == "bot_command" } -> Response(
      Status.BAD_REQUEST
    )

    else                                                       -> {
      val text = payload.message.text.removePrefix("/add").trim()
      val url = InputUrl.ofOrNull(text)
      followArtist(url!!) // TODO: answer back to caller
      sendMessage(TelegramMessage(text), payload.userId)
      Response(OK)
    }
  }
}

fun TelegramSecurityFilter(
  users: List<UserId>,
  secret: TelegramConfig.IncomingTelegramRequestSecret
) = Filter { next ->
  { req ->
    val payload = telegramCommand(req).message

    when {
      !req.has(secret)                                   -> Response(Status.UNAUTHORIZED)
      !users.contains(UserId(payload.from.id))           -> Response(Status.UNAUTHORIZED)
      payload.entities.none { it.type == "bot_command" } -> Response(Status.BAD_REQUEST)
      else                                               -> next(req)
    }
  }
}

// TODO use header lens
private fun Request.has(secret: TelegramConfig.IncomingTelegramRequestSecret) =
  header(TelegramConfig.TELEGRAM_SECRET_HEADER) != null
    && header(TelegramConfig.TELEGRAM_SECRET_HEADER) == secret.value
