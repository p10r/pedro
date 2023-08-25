package de.p10r.telegram

import de.p10r.UserId
import de.p10r.telegram.TelegramConfig.BotId
import de.p10r.telegram.TelegramConfig.BotSecret
import de.p10r.telegram.TelegramConfig.TelegramSecret
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.events.Event
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class TelegramClientTest {
  val config = TelegramConfig(
    botId = BotId("123"),
    botSecret = BotSecret("456"),
    secret = TelegramSecret("secret")
  )
  val uri = Uri.of("http://telegram")
  val events = mutableListOf<Event>()

  @Test
  fun `sends message`() {
    val chats = mapOf<UserId, MutableList<TelegramMessage>>(
      UserId(1) to mutableListOf(),
      UserId(2) to mutableListOf(),
    )
    val telegram = FakeTelegramServer(config, chats)
    val client = TelegramClient(uri, telegram, config, events::add)
    val userId = UserId(2)

    val res = client.sendMessage(TelegramMessage("hello there"), userId)

    expectThat(res.status).isEqualTo(OK)
    expectThat(chats[UserId(2)]).isEqualTo(mutableListOf(TelegramMessage("hello there")))
  }

  @Test
  fun `handles and monitors error`() {
    val error = { _: Request -> chatNotFoundError }
    val client = TelegramClient(uri, error, config, events::add)

    val response = client.sendMessage(TelegramMessage("hi"), UserId(666))

    expectThat(response).isEqualTo(Response(BAD_REQUEST).body("Bad Request: chat not found"))
    expectThat(events).hasSize(1)
    expectThat(events.first()).isA<OutgoingTelegramMessageError>()
  }

  @Test
  fun `monitoring works`() {
    val error = { _: Request -> chatNotFoundError }
    val success = { _: Request -> Response(OK) }

    TelegramClient(uri, error, config, events::add).sendMessage(
      TelegramMessage("hi"),
      UserId(666)
    )
    TelegramClient(uri, success, config, events::add).sendMessage(
      TelegramMessage("hi"),
      UserId(666)
    )

    expectThat(events).hasSize(2)
    expectThat(events.first()).isA<OutgoingTelegramMessageError>()
    expectThat(events.last()).isA<OutgoingTelegramMessage>()
  }
}

val chatNotFoundError = Response(BAD_REQUEST).body(
  """{
            "ok": false,
            "error_code": 400,
            "description": "Bad Request: chat not found"
        }""".trimIndent()
)

fun FakeTelegramServer(
  config: TelegramConfig,
  userChats: Map<UserId, MutableList<TelegramMessage>>
): RoutingHttpHandler {
  return ServerFilters.CatchLensFailure.then(
    routes("/bot${config.botId}:${config.botSecret}/sendMessage" bind POST to { req: Request ->
      val userId = chatIdLens(req)
      val message = messageLens(req)
      userChats[userId]
        ?.add(message)
        ?.let { Response(OK) }
        ?: chatNotFoundError
    }
    )
  )
}
