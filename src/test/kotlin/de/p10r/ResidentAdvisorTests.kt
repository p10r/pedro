package de.p10r

import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.with
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import strikt.api.expectThat
import strikt.assertions.isEqualTo

abstract class ResidentAdvisorContract(
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
}

@EnabledIfSystemProperty(named = "run-e2e", matches = "true")
class ProdResidentAdvisorTests : ResidentAdvisorContract(Uri.of("https://ra.co"), JavaHttpClient())

class ResidentAdvisorTests : ResidentAdvisorContract(
  uri = Uri.of("http://resident-advisor"),
  client = FakeRAServer(
    mapOf(
      RASlug("boysnoize") to RAArtistResponse(
        RAArtistResponse.RAData(
          RAArtist("943", "Boys Noize")
        )
      )
    )
  )
)

fun FakeRAServer(
  artistBySlugId: Map<RASlug, RAArtistResponse>
): RoutingHttpHandler {
  return routes(
    "/graphql" bind Method.POST to { request ->
      val matchingSlug = artistBySlugId.keys.find { it.toGetArtistQuery() == request.bodyString() }
      val artist =
        if (matchingSlug == null) null
        else artistBySlugId[matchingSlug]

      if (artist == null) {
        Response(Status.OK).with(raArtistResponse of RAArtistResponse(RAArtistResponse.RAData(null)))
      } else {
        Response(Status.OK).with(raArtistResponse of artist)
      }
    }
  )
}
