package de.p10r.scenarios

import de.p10r.fixtures.TelegramUser
import de.p10r.inputUrlOf
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEmpty

interface UseFromTelegramScenario {
  val telegramUser: TelegramUser

  @Test
  fun `follow and list a new artist`() {
    expectThat(telegramUser.listArtists()).isEmpty()

    telegramUser.followArtist(inputUrlOf("http://ra.co/dj/justice"))

    expectThat(telegramUser.listArtists()).hasSize(1)
    expectThat(telegramUser.listArtists().map { it.name })
      .contains("Justice")
  }
}
