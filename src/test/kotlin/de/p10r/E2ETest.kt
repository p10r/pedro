package de.p10r

import de.p10r.fixtures.E2ETestApp
import de.p10r.fixtures.Pedro
import de.p10r.fixtures.TestApp
import de.p10r.telegram.FakeTelegramServer
import de.p10r.telegram.IncomingTelegramRequest
import de.p10r.telegram.TelegramConfig
import de.p10r.telegram.TelegramConfig.BotId
import de.p10r.telegram.TelegramConfig.BotSecret
import de.p10r.telegram.TelegramConfig.Companion.TELEGRAM_SECRET_HEADER
import de.p10r.telegram.TelegramConfig.IncomingTelegramRequestSecret
import de.p10r.telegram.TelegramMessage
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.with
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class E2ETest {
  val validUserId = UserId(444)
  val botId = BotId("123")
  val botSecret = BotSecret("456")
  val chats = mapOf<UserId, MutableList<TelegramMessage>>(
    UserId(1) to mutableListOf(),
    validUserId to mutableListOf(),
  )

  val telegramServer = FakeTelegramServer(botId, botSecret, chats)
  val config = TelegramConfig.of(
    botId = botId,
    botSecret = botSecret,
    secret = IncomingTelegramRequestSecret("secret"),
    events = {},
    outgoingHttp = telegramServer,
    uri = Uri.of("http://localtelegram")
  )

  val app = TestApp(
    users = listOf(validUserId),
    telegramConfig = config
  )

  @Test
  fun `echoes message`() {
    val req = Request(POST, "/telegram")
      .header(TELEGRAM_SECRET_HEADER, config.secret.value)
      .with(
        telegramCommand of IncomingTelegramRequest(
          IncomingTelegramRequest.Message(
            IncomingTelegramRequest.Message.From(validUserId.value),
            text = "/add https://ra.co/dj/sabura",
            listOf(IncomingTelegramRequest.Message.Entity("bot_command"))
          )
        )
      )
    expectThat(app.getAllArtists()).isEmpty()

    val postMessageRes = app(req)

    expectThat(postMessageRes.status).isEqualTo(OK)
//    expectThat(app.getAllArtists()).isNotEmpty()
    expectThat(chats[validUserId])
      .isEqualTo(mutableListOf(TelegramMessage("/add https://ra.co/dj/sabura")))
  }

  @Test
  fun `lists artists`() {
    val app: Pedro = E2ETestApp()

    app.listArtists().also(::println)
  }

  private fun HttpHandler.getAllArtists() = this(Request(GET, "/artists")).let(artists)
}
