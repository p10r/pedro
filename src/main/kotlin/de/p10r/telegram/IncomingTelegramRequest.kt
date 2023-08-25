package de.p10r.telegram

data class IncomingTelegramRequest(
  val message: Message
) {
  data class Message(
    val from: From,
    val text: String,
    val entities: List<Entity>
  ) {
    data class From(val id: Int)
    data class Entity(val type: String)
  }
}
