package de.p10r.infrastructure

import de.p10r.App
import de.p10r.DynamoDbConfig
import de.p10r.UserId
import de.p10r.telegram.TelegramConfig
import okhttp3.OkHttpClient
import org.http4k.aws.AwsCredentials
import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.events.Event
import org.http4k.events.Events
import org.http4k.lens.string
import org.http4k.lens.uri
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.system.measureTimeMillis

private val DYNAMO_URI = EnvironmentKey.uri()
  .required("DYNAMO_URI")

private val DYNAMO_ID = EnvironmentKey.string()
  .required("DYNAMO_ID")

private val DYNAMO_SECRET = EnvironmentKey.string()
  .required("DYNAMO_SECRET")

private val TELEGRAM_URI = EnvironmentKey.uri()
  .defaulted("TELEGRAM_URI", Uri.of("https://api.telegram.org"))

private val TELEGRAM_BOT_ID = EnvironmentKey.string()
  .map(TelegramConfig::BotId)
  .required("TELEGRAM_BOT_ID")

private val TELEGRAM_BOT_SECRET = EnvironmentKey.string()
  .map(TelegramConfig::BotSecret)
  .required("TELEGRAM_BOT_SECRET")

private val TELEGRAM_REQ_SECRET = EnvironmentKey.string()
  .map(TelegramConfig::IncomingTelegramRequestSecret)
  .required("TELEGRAM_REQ_SECRET")

private val TELEGRAM_USER_IDS = EnvironmentKey.map { UserId(it.toInt()) }
  .multi
  .required("TELEGRAM_USER_IDS")

private val RA_URI = EnvironmentKey.uri()
  .defaulted("RA_URI", Uri.of("https://ra.co"))

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
      botId = TELEGRAM_BOT_ID(env),
      botSecret = TELEGRAM_BOT_SECRET(env),
      secret = TELEGRAM_REQ_SECRET(env),
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
      users = TELEGRAM_USER_IDS(env),
      features = Features()
    )
  } catch (e: Exception) {
    events(UncaughtExceptionEvent(e))
    throw e
  }
}
