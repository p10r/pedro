package de.p10r.adapters.driving

import de.p10r.domain.ArtistName
import de.p10r.domain.UserCommand.FollowArtist
import de.p10r.domain.UserCommand.ListArtists
import de.p10r.domain.UserId
import de.p10r.incomingTelegramReqOf
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class IncomingTelegramRequestTests {
  @Test
  fun `maps to TelegramCommand`() {
    expectThat(incomingTelegramReqOf(UserId(1), "/add http://ra.co/dj/mom").toCommand())
      .isEqualTo(FollowArtist(UserId(1), ArtistName.of("mom")))

    expectThat(incomingTelegramReqOf(UserId(1), "/add mom").toCommand())
      .isEqualTo(FollowArtist(UserId(1), ArtistName.of("mom")))

    expectThat(incomingTelegramReqOf(UserId(1), "/list").toCommand())
      .isEqualTo(ListArtists(UserId(1)))

    expectThat(incomingTelegramReqOf(UserId(1), "/idk").toCommand())
      .isEqualTo(null)
  }
}
