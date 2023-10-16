package de.p10r.adapters.driven.telegram

import de.p10r.domain.UserId
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.events.Event
import org.http4k.events.Events
import org.http4k.format.Jackson.auto
import org.http4k.lens.BiDiLens
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.string

val chatIdLens: BiDiLens<Request, UserId> =
  Query.int().map(::UserId, UserId::value).required("chat_id")

val errorLens =
  Body.auto<TelegramError>().toLens()

val messageLens =
  Query.string().map(::TelegramMessage, TelegramMessage::value).required("text")

class TelegramClient(
  private val config: TelegramConfig,
  private val events: Events
) {
  fun sendMessage(message: TelegramMessage, userId: UserId): Response {
    val req = Request(POST, "/bot${config.botId}:${config.botSecret}/sendMessage")
      .with(chatIdLens of userId, messageLens of message)
    val res = config.outgoingHttp(req)

    if (res.status.successful) {
      events(OutgoingTelegramMessage(message, userId))
      return res
    }

    events(OutgoingTelegramMessageError(req, res))
    return Response(res.status)
      .body(errorLens(res).description)
  }
}

data class OutgoingTelegramMessage(
  val telegramMessage: TelegramMessage,
  val userId: UserId
) : Event

data class OutgoingTelegramMessageError(
  val req: Request,
  val res: Response
) : Event

data class TelegramMessage(val value: String)

@Suppress("PropertyName")
data class TelegramError(
  val ok: Boolean,
  val error_code: Int,
  val description: String
)
