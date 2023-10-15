package de.p10r

import de.p10r.adapters.driving.IncomingTelegramRequest
import de.p10r.domain.Artist
import de.p10r.domain.NewArtist
import strikt.api.Assertion
import strikt.java.propertiesAreEqualToIgnoring

fun incomingTelegramReqOf(
  userId: UserId,
  text: String
) = IncomingTelegramRequest(
  IncomingTelegramRequest.Message(
    IncomingTelegramRequest.Message.From(userId.value),
    text = text,
    listOf(IncomingTelegramRequest.Message.Entity("bot_command"))
  )
)


fun List<Artist>.toNewArtists() = map { it.toNewArtist() }
fun Artist.toNewArtist() = NewArtist(name)

fun readTextFrom(path: String) = {}::class.java.classLoader.getResource(path)!!.readText()

fun Assertion.Builder<Artist>.isEqualToIgnoringId(other: Artist) =
  this.propertiesAreEqualToIgnoring(other, Artist::id)

