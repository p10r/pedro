package de.p10r

import dev.forkhandles.values.ofOrNull
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ArtistIdTests {

  @Test
  fun `builds id`() {
    expectThat(ArtistId.ofOrNull(1234)?.id)
      .isEqualTo("art-1234")
  }

  @Test
  fun `accepts only positive numbers`() {
    expectThat(ArtistId.ofOrNull(-1234)).isNull()
  }
}
