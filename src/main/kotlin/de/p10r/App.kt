package de.p10r

import de.p10r.infrastructure.AppIncomingHttp
import de.p10r.infrastructure.AppOutgoingHttp
import de.p10r.infrastructure.Features
import de.p10r.infrastructure.UncaughtExceptionEvent
import de.p10r.ra.RAClient
import de.p10r.telegram.IncomingTelegramRequest
import de.p10r.telegram.TelegramClient
import de.p10r.telegram.TelegramConfig
import de.p10r.telegram.TelegramConfig.Companion.TELEGRAM_SECRET_HEADER
import de.p10r.telegram.TelegramConfig.IncomingTelegramRequestSecret
import de.p10r.telegram.TelegramMessage
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.events.Events
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes

private val inputUrl = FormField.map { InputUrl.ofOrNull(it) }.required("url")
private val idFrom = Body.webForm(Validator.Feedback, inputUrl).toLens()
val artists = Body.auto<List<Artist>>().toLens()
val telegramCommand = Body.auto<IncomingTelegramRequest>().toLens()

data class UserId(val value: Int)

fun App(
  database: Database,
  raUri: Uri,
  raHttp: HttpHandler,
  events: Events,
  telegramConfig: TelegramConfig,
  users: List<UserId>,
  @Suppress("UNUSED_PARAMETER") features: Features,
): HttpHandler {
  val artistRepository = ArtistRepository(database)
  val raClient = RAClient(raUri, AppOutgoingHttp(events, raHttp))
  val artistsRegistry = ArtistsRegistry(artistRepository, raClient)
  //TODO remove events as last parameter
  val telegramClient = TelegramClient(telegramConfig, events)


  return AppIncomingHttp(
    events,
    ServerFilters.CatchAll {
      events(UncaughtExceptionEvent(it))
      Response(Status.INTERNAL_SERVER_ERROR)
    }.then(AppRoutes(artistsRegistry, telegramConfig.secret, users, telegramClient))
  )
}

private fun AppRoutes(
  artistsRegistry: ArtistsRegistry,
  secret: IncomingTelegramRequestSecret,
  users: List<UserId>,
  telegramClient: TelegramClient,
) = routes(
  "/artists" bind POST to { req ->
    idFrom(req).let(inputUrl)?.let {
      artistsRegistry.add(it)
      Response(Status.SEE_OTHER).header("Location", "/")
    } ?: Response(BAD_REQUEST)
  },
  "/artists" bind GET to {
    Response(OK).with(artists of artistsRegistry.list())
  },
  "/ping" bind GET to { Response(OK).body("pong") },
  "/telegram" bind POST to TelegramSecurityFilter(users, secret).then { req ->
    val msg = telegramCommand(req)

    when {
      msg.message.entities.none { it.type == "bot_command" } -> Response(BAD_REQUEST)
      else                                                   -> {
        telegramClient.sendMessage(TelegramMessage(msg.message.text), msg.userId)
        Response(OK)
      }
    }
  }
)

fun TelegramSecurityFilter(
  users: List<UserId>,
  secret: IncomingTelegramRequestSecret
) = Filter { next ->
  { req ->
    val payload = telegramCommand(req).message

    when {
      !req.has(secret)                                   -> Response(UNAUTHORIZED)
      !users.contains(UserId(payload.from.id))           -> Response(UNAUTHORIZED)
      payload.entities.none { it.type == "bot_command" } -> Response(BAD_REQUEST)
      else                                               -> next(req)
    }
  }
}

// TODO use header lens
private fun Request.has(secret: IncomingTelegramRequestSecret) =
  header(TELEGRAM_SECRET_HEADER) != null
    && header(TELEGRAM_SECRET_HEADER) == secret.value
