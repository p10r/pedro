package de.p10r.scenarios

import de.p10r.adapters.driving.ArtistResponse
import de.p10r.fixtures.TelegramUser
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

interface TelegramUserScenarios {
  val telegramUser: TelegramUser

  @Test
  fun `follow and list a new artist`() {
    expectThat(telegramUser.listArtists()).isEmpty()

    telegramUser.followArtist("http://ra.co/dj/justice")

    expectThat(telegramUser.listArtists())
      .containsExactly(ArtistResponse("Justice"))
  }

  @Test
  fun `follow a new artist`() {
    expectThat(telegramUser.listArtists()).isEmpty()

    telegramUser.followArtist("http://ra.co/dj/justice")

    expectThat(telegramUser.listArtists())
      .containsExactly(ArtistResponse("Justice"))
  }
}
