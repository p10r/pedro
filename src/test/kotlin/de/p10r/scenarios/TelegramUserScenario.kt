package de.p10r.scenarios

import de.p10r.fixtures.TelegramUser
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize

interface TelegramUserScenario {
  val telegramUser: TelegramUser

  @Test
  fun `view all artists pedro is aware of`() {
    expectThat(telegramUser.listArtists()).hasSize(1)
  }
}
