package de.p10r

import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.webdriver.Http4kWebDriver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By

@ExtendWith(ApprovalTest::class)
class UiTests {
  val app = TestApp(
    listOf(
      NewArtist("Boys Noize"),
      NewArtist("Justin Tinderdate"),
      NewArtist("Sinamin"),
    ),
    raServer = FakeRAServer(
      mapOf(
        RASlug("justice") to RAArtistResponse(
          RAArtistResponse.RAData(
            RAArtist("777", "Justice")
          )
        )
      )
    )

  )
  val indexPage = indexPage()

  @Test
  fun `lists already stored artists`(approver: Approver) {
    assertNotNull(indexPage.findElement(By.id("artist-1")))
    assertNotNull(indexPage.findElement(By.id("artist-2")))
    assertNotNull(indexPage.findElement(By.id("artist-3")))
  }

  @Test
  fun `add artist from resident advisor`() {
    indexPage.findElement(By.id("url"))
      ?.sendKeys("https://ra.co/dj/justice")

    indexPage.findElement(By.id("add-artist-submit"))?.submit()

    assertEquals(
      "Justice",
      indexPage.findElement(By.id("artist-4"))?.text
    )
  }

  private fun indexPage() = Http4kWebDriver(app).apply {
    navigate().to("/")
  }
}
