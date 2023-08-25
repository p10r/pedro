package de.p10r.telegram

data class TelegramConfig(
  val botId: BotId,
  val botSecret: BotSecret,
  val secret: TelegramSecret
) {
  companion object {
    const val TELEGRAM_SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token"
  }

  data class BotId(val value: String)
  data class BotSecret(val value: String)
  data class TelegramSecret(val value: String)
}

