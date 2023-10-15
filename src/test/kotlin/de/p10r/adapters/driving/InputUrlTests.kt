package de.p10r.adapters.driving

import de.p10r.domain.InputUrl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class InputUrlTests {
  @Test
  fun `denies invalid input url`() {
    assertNull(InputUrl.ofOrNull("this_is_not_a_url"))
  }

  @Test
  fun `allows valid urls`() {
    assertNotNull(InputUrl.ofOrNull("ra.co/dj/boysnoize"))
    assertNotNull(InputUrl.ofOrNull("http://ra.co/dj/boysnoize"))
    assertNotNull(InputUrl.ofOrNull("https://ra.co/dj/boysnoize"))
  }
}
