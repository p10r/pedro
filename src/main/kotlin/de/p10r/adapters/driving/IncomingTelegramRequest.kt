package de.p10r.adapters.driving

import de.p10r.UserId
import de.p10r.domain.TelegramCommand

data class IncomingTelegramRequest(
  val message: Message
) {
  fun toCommand(): TelegramCommand? {
    val text = message.text

    if (text.startsWith("/add")) {
      val input = text
        .removePrefix("/add")
        .trim()

      val baseUrl = "http://ra.co/dj/"
      val sanitized =
        if (input.startsWith(baseUrl)) input.removePrefix(baseUrl)
        else input

      return TelegramCommand.AddArtist(sanitized)
    }

    if (text.startsWith("/list"))
      return TelegramCommand.ListArtists

    return null
  }

  val userId = UserId(message.from.id)

  data class Message(
    val from: From,
    val text: String,
    val entities: List<Entity>
  ) {
    data class From(val id: Int)
    data class Entity(val type: String)
  }
}
