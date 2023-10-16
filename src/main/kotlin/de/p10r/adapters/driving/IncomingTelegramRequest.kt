package de.p10r.adapters.driving

import de.p10r.domain.UserCommand
import de.p10r.domain.UserId

data class IncomingTelegramRequest(val message: Message) {
  val userId = UserId(message.from.id)

  data class Message(
    val from: From,
    val text: String,
    val entities: List<Entity>
  ) {
    data class From(val id: Int)
    data class Entity(val type: String)
  }

  fun toCommand(): UserCommand? {
    val text = message.text

    if (text.startsWith("/add")) {
      val input = text
        .removePrefix("/add")
        .trim()

      val baseUrl = "http://ra.co/dj/"
      val sanitized =
        if (input.startsWith(baseUrl)) input.removePrefix(baseUrl)
        else input

      return UserCommand.FollowArtist(userId, sanitized)
    }

    if (text.startsWith("/list"))
      return UserCommand.ListArtists(userId)

    return null
  }
}
