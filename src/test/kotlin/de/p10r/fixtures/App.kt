package de.p10r.fixtures

import de.p10r.adapters.driven.ra.RAArtistResponse
import de.p10r.adapters.driven.ra.RASlug
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driven.telegram.TelegramMessage
import de.p10r.domain.UserId
import de.p10r.infrastructure.PedroSettings.DYNAMO_ID
import de.p10r.infrastructure.PedroSettings.DYNAMO_SECRET
import de.p10r.infrastructure.PedroSettings.DYNAMO_URI
import de.p10r.infrastructure.PedroSettings.RA_URI
import de.p10r.infrastructure.PedroSettings.TELEGRAM_BOT_ID
import de.p10r.infrastructure.PedroSettings.TELEGRAM_BOT_SECRET
import de.p10r.infrastructure.PedroSettings.TELEGRAM_REQ_SECRET
import de.p10r.infrastructure.PedroSettings.TELEGRAM_URI
import de.p10r.infrastructure.PedroSettings.TELEGRAM_USER_IDS
import de.p10r.infrastructure.ProdApp
import de.p10r.infrastructure.loggingEvents
import de.p10r.infrastructure.then
import de.p10r.telegram.FakeTelegramServer
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.routing.reverseProxyRouting
import org.http4k.routing.routes


class App(
  telegramChats: Map<UserId, MutableList<TelegramMessage>>,
  raArtists: Map<RASlug, RAArtistResponse>,
) : HttpHandler {
  val events = loggingEvents() then { }

  private val env = Environment.defaults(
    DYNAMO_URI of Uri.of("http://dynamo-db"),
    DYNAMO_ID of "accessKey",
    DYNAMO_SECRET of "secret",
    TELEGRAM_URI of Uri.of("http://telegram"),
    TELEGRAM_BOT_ID of "telegram-bot-id",
    TELEGRAM_BOT_SECRET of "telegram-bot-secret",
    TELEGRAM_REQ_SECRET of "telegram-request-secret",
    TELEGRAM_USER_IDS of telegramChats.keys.map { it.value }.joinToString(","),
    RA_URI of Uri.of("http://resident-advisor"),
  )

  private val networkAccess = NetworkAccess()
  private val telegram = FakeTelegramServer(
    TelegramConfig.BotId(TELEGRAM_BOT_ID(env)),
    TelegramConfig.BotSecret(TELEGRAM_BOT_SECRET(env)),
    telegramChats
  )
  private val dynamoDb = FakeDynamoDb()
  private val dynamoDbClient = dynamoDb.client()
  private val ra = FakeRAServer(raArtists)
  private val pedro = ProdApp(env, events, networkAccess)

  init {
    dynamoDbClient.createPedroTables()

    networkAccess.http = routes(
      reverseProxyRouting(
        env[DYNAMO_URI].authority to dynamoDb,
        env[TELEGRAM_URI].authority to telegram,
        env[RA_URI].authority to ra,
        Uri.of("http://pedro").authority to pedro,
      )
    )
  }

  override fun invoke(req: Request): Response = networkAccess(req)
}

/**
 * Combines all HTTP routing into a single place.
 */
private class NetworkAccess : HttpHandler {
  var http: HttpHandler? = null

  override fun invoke(req: Request) =
    http?.invoke(req)
      ?: Response(Status.NOT_IMPLEMENTED)
        .body("No mapped host for: ${req.uri}")
}
