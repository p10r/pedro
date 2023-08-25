package de.p10r.telegram

data class TelegramConfig(
  val botId: BotId,
  val botSecret: BotSecret
) {
  data class BotId(val value: String)
  data class BotSecret(val value: String)
}
