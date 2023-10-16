package de.p10r.telegram

import de.p10r.adapters.driven.telegram.OutgoingTelegramMessageError
import de.p10r.adapters.driven.telegram.TelegramClient
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driven.telegram.TelegramConfig.BotId
import de.p10r.adapters.driven.telegram.TelegramConfig.BotSecret
import de.p10r.adapters.driven.telegram.TelegramConfig.IncomingTelegramRequestSecret
import de.p10r.adapters.driven.telegram.TelegramMessage
import de.p10r.adapters.driven.telegram.chatIdLens
import de.p10r.adapters.driven.telegram.messageLens
import de.p10r.domain.models.UserId
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
  val config = TelegramConfig.of(
    botId = BotId("123"),
    botSecret = BotSecret("456"),
    secret = IncomingTelegramRequestSecret("secret"),
    events = {},
    outgoingHttp = { _: Request -> Response(OK) },
    uri = Uri.of("http://localtelegram")
  )
  val events = mutableListOf<Event>()

  @Test
  fun `sends message`() {
    val chats = mapOf<UserId, MutableList<TelegramMessage>>(
      UserId(1) to mutableListOf(),
      UserId(2) to mutableListOf(),
    )
    val telegram = FakeTelegramServer(config.botId, config.botSecret, chats)
    val client = TelegramClient(config.copy(outgoingHttp = telegram), events::add)
    val userId = UserId(2)

    val res = client.sendMessage(TelegramMessage("hello there"), userId)

    expectThat(res.status).isEqualTo(OK)
    expectThat(chats[UserId(2)]).isEqualTo(mutableListOf(TelegramMessage("hello there")))
  }

  @Test
  fun `handles error`() {
    val error = { _: Request -> chatNotFoundError }
    val client = TelegramClient(config.copy(outgoingHttp = error), events::add)

    val response = client.sendMessage(TelegramMessage("hi"), UserId(666))

    expectThat(response).isEqualTo(Response(BAD_REQUEST).body("Bad Request: chat not found"))
    expectThat(events).hasSize(1)
    expectThat(events.first()).isA<OutgoingTelegramMessageError>()
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
  botId: BotId,
  botSecret: BotSecret,
  userChats: Map<UserId, MutableList<TelegramMessage>>
): RoutingHttpHandler {
  return ServerFilters.CatchLensFailure.then(
    routes("/bot$botId:$botSecret/sendMessage" bind POST to { req: Request ->
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
