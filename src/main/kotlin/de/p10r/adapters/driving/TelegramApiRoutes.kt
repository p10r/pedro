package de.p10r.adapters.driving

import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.domain.UserCommand
import de.p10r.domain.UserCommandResult
import de.p10r.domain.UserCommandResult.AddedArtist
import de.p10r.domain.UserCommandResult.Artists
import de.p10r.domain.UserId
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import org.http4k.core.with

fun TelegramApi(
  users: List<UserId>,
  secret: TelegramConfig.IncomingTelegramRequestSecret,
  process: (UserCommand) -> UserCommandResult
) = TelegramSecurityFilter(users, secret).then { req ->
  val payload = telegramReq(req)

  if (payload.message.entities.none { it.type == "bot_command" })
    return@then Response(BAD_REQUEST)

  val userCommand = payload.toCommand()
    ?: return@then Response(BAD_REQUEST)

  return@then when (val result = process(userCommand)) {
    is AddedArtist -> Response(OK)
    is Artists     -> Response(OK).with(artists of result.artists.toResponse())
  }
}

fun TelegramSecurityFilter(
  users: List<UserId>,
  secret: TelegramConfig.IncomingTelegramRequestSecret
) = Filter { next ->
  { req ->
    val payload = telegramReq(req)

    when {
      !req.has(secret)                                           -> Response(UNAUTHORIZED)
      !users.contains(payload.userId)                            -> Response(UNAUTHORIZED)
      payload.message.entities.none { it.type == "bot_command" } -> Response(BAD_REQUEST)
      else                                                       -> next(req)
    }
  }
}

// TODO use header lens
private fun Request.has(secret: TelegramConfig.IncomingTelegramRequestSecret) =
  header(TelegramConfig.TELEGRAM_SECRET_HEADER) != null
    && header(TelegramConfig.TELEGRAM_SECRET_HEADER) == secret.value
