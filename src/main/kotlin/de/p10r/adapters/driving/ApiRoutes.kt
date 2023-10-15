package de.p10r.adapters.driving

import de.p10r.UserId
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.domain.Artist
import de.p10r.domain.TelegramCommand
import de.p10r.domain.TelegramCommandResult
import de.p10r.infrastructure.AppIncomingHttp
import de.p10r.infrastructure.UncaughtExceptionEvent
import org.http4k.core.Body
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

val artists = Body.auto<List<ArtistResponse>>().toLens()
fun List<Artist>.toResponse() = map { ArtistResponse(it.name) }
val telegramReq = Body.auto<IncomingTelegramRequest>().toLens()

fun ApiRoutes(
  processTelegramCommands: (TelegramCommand) -> TelegramCommandResult,
  listAllArtists: () -> List<Artist>,
  secret: TelegramConfig.IncomingTelegramRequestSecret,
  users: List<UserId>,
  events: Events,
) = AppIncomingHttp(
  events,
  ServerFilters.CatchAll {
    events(UncaughtExceptionEvent(it))
    Response(Status.INTERNAL_SERVER_ERROR)
  }.then(
    routes(
      "/artists" bind GET to ListArtists(listAllArtists),
      "/telegram" bind POST to TelegramApi(users, secret, processTelegramCommands)
    )
  )
)

private fun ListArtists(
  listAllArtists: () -> List<Artist>
) = { _: Request -> Response(OK).with(artists of listAllArtists().toResponse()) }
