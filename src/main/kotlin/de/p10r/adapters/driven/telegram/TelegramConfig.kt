package de.p10r.adapters.driven.telegram

import de.p10r.infrastructure.AppOutgoingHttp
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.events.Events
import org.http4k.filter.ClientFilters

data class TelegramConfig private constructor(
  val outgoingHttp: HttpHandler,
  val botId: BotId,
  val botSecret: BotSecret,
  val secret: IncomingTelegramRequestSecret
) {
  companion object {
    const val TELEGRAM_SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token"

    fun of(
      uri: Uri,
      outgoingHttp: HttpHandler,
      botId: BotId,
      botSecret: BotSecret,
      secret: IncomingTelegramRequestSecret,
      events: Events
    ): TelegramConfig {
      val http = ClientFilters.SetBaseUriFrom(uri).then(outgoingHttp)

      return TelegramConfig(
        outgoingHttp = AppOutgoingHttp(events, http),
        botId,
        botSecret,
        secret,
      )
    }
  }

  data class BotId(val value: String)
  data class BotSecret(val value: String)
  data class IncomingTelegramRequestSecret(val value: String)
}

