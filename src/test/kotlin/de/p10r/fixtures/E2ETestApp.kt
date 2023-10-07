package de.p10r.fixtures

import de.p10r.Artist
import de.p10r.artists
import de.p10r.infrastructure.ProdApp
import de.p10r.telegram.FakeTelegramServer
import de.p10r.telegram.TelegramConfig
import org.http4k.chaos.start
import org.http4k.client.JavaHttpClient
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer

interface Pedro {
  fun listArtists(): List<Artist>
}

class E2ETestApp : Pedro {
  private val pedro = start()
  private val client = ClientFilters
    .SetBaseUriFrom(Uri.of("http://localhost:${pedro.port()}"))
    .then(JavaHttpClient())

  private fun start(): Http4kServer {
    val telegramBotId = TelegramConfig.BotId("secure-bot-id")
    val telegramBotSecret = TelegramConfig.BotSecret("telegram-bot-s3cr3t")
    val telegramReqSecret = "idk"
    val telegram = FakeTelegramServer(
      botId = telegramBotId,
      botSecret = telegramBotSecret,
      userChats = mapOf()
    ).asServer(SunHttp(port = 0)).start()

    val ra = FakeRAServer(mapOf()).asServer(SunHttp(port = 0)).start()

    val dynamoDb = FakeDynamoDb().start()

    val env = Environment.from(
      mapOf(
        "DYNAMO_URI" to "http://localhost:${dynamoDb.port()}",
        "DYNAMO_ID" to "accessKey",
        "DYNAMO_SECRET" to "secret",
        "TELEGRAM_URI" to "http://localhost:${telegram.port()}",
        "TELEGRAM_BOT_ID" to telegramBotId.value,
        "TELEGRAM_BOT_SECRET" to telegramBotSecret.value,
        "TELEGRAM_REQ_SECRET" to telegramReqSecret,
        "TELEGRAM_USER_IDS" to "1,2,3",
        "RA_URI" to "http://localhost:${ra.port()}",
      )
    )

    return ProdApp(env, events = {}).asServer(SunHttp(port = 0)).start()
  }

  override fun listArtists(): List<Artist> {
    val res = client(Request(Method.GET, "/artists"))
    return artists(res)
  }
}
