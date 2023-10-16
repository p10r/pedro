package de.p10r.scenarios

import de.p10r.adapters.driving.ArtistResponse
import de.p10r.fixtures.TelegramUser
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

interface TelegramUserScenarios {
  val sarah: TelegramUser
  val joe: TelegramUser

  @Test
  fun `follow and list a new artist`() {
    expectThat(sarah.listArtists()).isEmpty()

    sarah.followArtist("http://ra.co/dj/justice")

    expectThat(sarah.listArtists())
      .containsExactly(ArtistResponse("Justice"))
  }

  @Test
  fun `follow a new artist`() {
    expectThat(sarah.listArtists()).isEmpty()

    sarah.followArtist("http://ra.co/dj/justice")

    expectThat(sarah.listArtists())
      .containsExactly(ArtistResponse("Justice"))
  }

  @Test
  fun `lists only artists for user`() {
    expectThat(sarah.listArtists()).isEmpty()
    expectThat(joe.listArtists()).isEmpty()

    sarah.followArtist("http://ra.co/dj/justice")
    sarah.followArtist("http://ra.co/dj/sabura")
    sarah.followArtist("http://ra.co/dj/boysnoize")
    joe.followArtist("http://ra.co/dj/boysnoize")

    expectThat(joe.listArtists())
      .containsExactly(ArtistResponse("Boys Noize"))
  }
}
