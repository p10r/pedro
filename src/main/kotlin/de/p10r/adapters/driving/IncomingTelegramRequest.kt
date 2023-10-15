package de.p10r.adapters.driving

import de.p10r.UserId

data class IncomingTelegramRequest(
  val message: Message
) {
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
