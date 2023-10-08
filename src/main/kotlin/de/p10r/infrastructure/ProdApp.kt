package de.p10r.infrastructure

import de.p10r.App
import de.p10r.DynamoDbConfig
import de.p10r.UserId
import de.p10r.infrastructure.PedroSettings.DYNAMO_ID
import de.p10r.infrastructure.PedroSettings.DYNAMO_SECRET
import de.p10r.infrastructure.PedroSettings.DYNAMO_URI
import de.p10r.infrastructure.PedroSettings.RA_URI
import de.p10r.infrastructure.PedroSettings.TELEGRAM_BOT_ID
import de.p10r.infrastructure.PedroSettings.TELEGRAM_BOT_SECRET
import de.p10r.infrastructure.PedroSettings.TELEGRAM_REQ_SECRET
import de.p10r.infrastructure.PedroSettings.TELEGRAM_URI
import de.p10r.infrastructure.PedroSettings.TELEGRAM_USER_IDS
import de.p10r.telegram.TelegramConfig
import de.p10r.telegram.TelegramConfig.BotId
import de.p10r.telegram.TelegramConfig.BotSecret
import de.p10r.telegram.TelegramConfig.IncomingTelegramRequestSecret
import okhttp3.OkHttpClient
import org.http4k.aws.AwsCredentials
import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.events.Event
import org.http4k.events.Events
import org.http4k.lens.of
import org.http4k.lens.uri
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.system.measureTimeMillis

object PedroSettings {
  val DYNAMO_URI by EnvironmentKey.uri().of().required()
  val DYNAMO_ID by EnvironmentKey.of().required()
  val DYNAMO_SECRET by EnvironmentKey.of().required()
  val TELEGRAM_URI by EnvironmentKey.uri().of().required()
  val TELEGRAM_BOT_ID by EnvironmentKey.of().required()
  val TELEGRAM_BOT_SECRET by EnvironmentKey.of().required()
  val TELEGRAM_REQ_SECRET by EnvironmentKey.of().required()
  val TELEGRAM_USER_IDS by EnvironmentKey.of().required()
  val RA_URI by EnvironmentKey.uri().of().required()
}

data class ServerStartedEvent(val ms: Long) : Event

fun main() {
  val events: (Event) -> Unit = loggingEvents()
  measureTimeMillis {
    ProdApp(Environment.ENV, events).asServer(SunHttp(8080)).start()
  }.let { events(ServerStartedEvent(it)) }
}

//TODO check connection
fun ProdApp(env: Environment, events: Events): HttpHandler {
  try {
    val client = OkHttpClient.Builder()
      .followRedirects(true)
      .build()
      .let { OkHttp(it) }

    val telegramConfig = TelegramConfig.of(
      uri = TELEGRAM_URI(env),
      outgoingHttp = client,
      botId = BotId(TELEGRAM_BOT_ID(env)),
      botSecret = BotSecret(TELEGRAM_BOT_SECRET(env)),
      secret = IncomingTelegramRequestSecret(TELEGRAM_REQ_SECRET(env)),
      events = events
    )

    val dynamoDbConfig = DynamoDbConfig(
      uri = DYNAMO_URI(env),
      http = client,
      credentials = AwsCredentials(DYNAMO_ID(env), DYNAMO_SECRET(env))
    )

    val raUri = RA_URI(env)

    return App(
      dynamoDbConfig = dynamoDbConfig,
      raUri = raUri,
      raHttp = client,
      events = events,
      telegramConfig = telegramConfig,
      users = TELEGRAM_USER_IDS(env).split(",").map { UserId(it.toInt()) },
      features = Features()
    )
  } catch (e: Exception) {
    events(UncaughtExceptionEvent(e))
    throw e
  }
}
