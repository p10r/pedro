package de.p10r.fixtures

import de.p10r.App
import de.p10r.UserId
import de.p10r.infrastructure.Features
import de.p10r.infrastructure.loggingEvents
import de.p10r.infrastructure.then
import de.p10r.ra.RAArtist
import de.p10r.ra.RAArtistResponse
import de.p10r.ra.RASlug
import de.p10r.telegram.TelegramConfig
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.events.Events
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.system.measureTimeMillis

fun main() {
  measureTimeMillis {
    TestApp().asServer(SunHttp(8080)).start()
  }.let { println("Server started in $it ms") }
}

fun TestApp(
  raUri: Uri = Uri.of("http://ra.co"),
  raServer: HttpHandler = RAServer(
    mapOf(
      RASlug("boysnoize") to RAArtistResponse(
        RAArtistResponse.RAData(
          RAArtist("943", "Boys Noize")
        )
      )
    )
  ),
  telegramConfig: TelegramConfig = TelegramConfig.of(
    botId = TelegramConfig.BotId("123"),
    botSecret = TelegramConfig.BotSecret("456"),
    secret = TelegramConfig.IncomingTelegramRequestSecret("secret"),
    events = {},
    outgoingHttp = { Response(Status.OK) },
    uri = Uri.of("http://localtelegram")
  ),
  users: List<UserId> = listOf(UserId(1)),
  events: Events = {},
): HttpHandler {

  return App(
    dynamoDbConfig = dynamoDbConfig,
    raUri = raUri,
    raHttp = raServer,
    events = loggingEvents() then events,
    telegramConfig = telegramConfig,
    features = Features(onlyPing = false),
    users = users,
  )
}
