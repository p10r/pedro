package de.p10r

import de.p10r.RAArtistResponse.RAData
import de.p10r.RAEventsResponse.RADataWrapper.RAListing.RAEvent
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import java.time.LocalDate
import java.time.Month

abstract class RAContract(
  val uri: Uri,
  val client: HttpHandler,
) {
  private val raClient = RAClient(uri, client)

  @Test
  fun `finds artist id by slug`() {
    expectThat(raClient.getArtistBy(RASlug("boysnoize")))
      .isEqualTo(RAArtist("943", "Boys Noize"))
  }

  @Test
  fun `returns null if artist can't be found`() {
    expectThat(RAClient(uri, client).getArtistBy(RASlug("this_artist_doesnt_exist")))
      .isEqualTo(null)
  }

  @Test
  fun `lists artists events`() {
    expectThat(
      raClient.getEventsFor(
        raArtist = RAArtist("943", "Boys Noize"),
        startDate = LocalDate.of(2023, Month.JUNE, 1)
      )
    ).contains(RAEvent("1708911"))
  }
}

//These tests might get outdated because the queries are set to specific dates
//Update the queries if needed
@EnabledIfSystemProperty(named = "run-e2e", matches = "true")
class ProdRATests : RAContract(Uri.of("https://ra.co"), JavaHttpClient())

class RATests : RAContract(
  uri = Uri.of("http://resident-advisor"),
  client = FakeRAServer(
    mapOf(
      RASlug("boysnoize") to RAArtistResponse(
        RAData(
          RAArtist("943", "Boys Noize")
        )
      )
    )
  )
)

