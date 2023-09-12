package de.p10r.telegram

import de.p10r.TelegramSecurityFilter
import de.p10r.UserId
import de.p10r.readTextFrom
import de.p10r.telegram.IncomingTelegramRequest.Message
import de.p10r.telegram.TelegramConfig.Companion.TELEGRAM_SECRET_HEADER
import de.p10r.telegram.TelegramConfig.TelegramSecret
import de.p10r.telegramCommand
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.with
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class TelegramApiTests {
  @Test
  fun `deserializes telegram request body`() {
    val req = Request(POST, "/").body(readTextFrom("incoming-telegram-request.json"))
      .header("ContentType", ContentType.APPLICATION_JSON.toHeaderValue())

    expectThat(telegramCommand(req)).isA<IncomingTelegramRequest>()
  }

  @Test
  fun `accepts only bot command`() {
    val filter = TelegramSecurityFilter(listOf(UserId(1)), TelegramSecret("secret"))
    val app = filter { Response(OK) }

    expectThat(app.postTelegramMessage(Message.Entity("bot_command"), Message.From(1)))
      .isEqualTo(Response(OK))

    expectThat(app.postTelegramMessage(Message.Entity("something_else"), Message.From(1)))
      .isEqualTo(Response(BAD_REQUEST))
  }

  @Test
  fun `returns 401 if user is not eligible for bot`() {
    val filter = TelegramSecurityFilter(listOf(UserId(111)), TelegramSecret("secret"))
    val app = filter { Response(OK) }

    expectThat(app.postTelegramMessage(from = Message.From(111)))
      .isEqualTo(Response(OK))

    expectThat(app.postTelegramMessage(from = Message.From(222)))
      .isEqualTo(Response(UNAUTHORIZED))
  }


  @Test
  fun `accepts only request with correct secret_token header`() {
    val filter = TelegramSecurityFilter(listOf(UserId(1)), TelegramSecret("s3cr3t"))
    val app = filter { Response(OK) }

    expectThat(
      app.postTelegramMessage(header = "X-Telegram-Bot-Api-Secret-Token" to "s3cr3t")
    ).isEqualTo(Response(OK))

    expectThat(
      app.postTelegramMessage(header = "X-Telegram-Bot-Api-Secret-Token" to "something else")
    ).isEqualTo(Response(UNAUTHORIZED))
  }

}

fun HttpHandler.postTelegramMessage(
  entity: Message.Entity = Message.Entity("bot_command"),
  from: Message.From = Message.From(id = 1),
  header: Pair<String, String> = TELEGRAM_SECRET_HEADER to "secret"
) = Request(POST, "/telegram")
  .header(header.first, header.second)
  .with(
    telegramCommand of IncomingTelegramRequest(
      Message(
        from,
        text = "/add my_fav_artist",
        listOf(entity)
      )
    )
  ).let(this)
