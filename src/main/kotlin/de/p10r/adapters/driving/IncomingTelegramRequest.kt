package de.p10r.adapters.driving

import de.p10r.domain.models.ArtistName
import de.p10r.domain.models.UserCommand
import de.p10r.domain.models.UserId

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

      return UserCommand.FollowArtist(userId, ArtistName.of(input))
    }

    if (text.startsWith("/list"))
      return UserCommand.ListArtists(userId)

    return null
  }
}
