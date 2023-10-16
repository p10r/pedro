package de.p10r

import de.p10r.adapters.driving.IncomingTelegramRequest
import de.p10r.domain.Artist
import de.p10r.domain.ArtistId
import de.p10r.domain.UserCommandResult
import de.p10r.domain.UserId
import strikt.api.Assertion
import strikt.api.DescribeableBuilder
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isA
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

fun readTextFrom(path: String) = {}::class.java.classLoader.getResource(path)!!.readText()

fun Assertion.Builder<List<Artist>>.containsExactlyInAnyOrderIgnoringIds(other: List<Artist>) {
  // This way we can be sure that we also check newly added properties
  val first = this.subject.map { it.copy(id = ArtistId.of("1")) }
  val second = other.map { it.copy(id = ArtistId.of("1")) }
  expectThat(first).containsExactlyInAnyOrder(second)
}

fun Assertion.Builder<Artist>.isEqualToIgnoringId(other: Artist) =
  this.propertiesAreEqualToIgnoring(other, Artist::id)

fun DescribeableBuilder<UserCommandResult>.isArtistsResult() =
  this.isA<UserCommandResult.Artists>()
    .get(UserCommandResult.Artists::artists)
