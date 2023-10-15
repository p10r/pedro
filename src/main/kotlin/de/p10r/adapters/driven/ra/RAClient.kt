package de.p10r.adapters.driven.ra

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.format.Jackson.auto
import java.time.LocalDate

val raArtistResponse = Body.auto<RAArtistResponse>().toLens()
val raEventsResponse = Body.auto<RAEventsResponse>().toLens()

class RAClient(
  raUri: Uri,
  http: HttpHandler
) {
  //The header is needed to make it work
  //TODO: write test to check user agent in fake
  private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
    " AppleWebKit/537.36 (KHTML, like Gecko)" +
    " Chrome/113.0.0.0 Safari/537.36"

  private val client = ClientFilters.SetBaseUriFrom(raUri).then(http)

  fun getArtistBy(slug: RASlug): RAArtist? {
    val request = Request(POST, "/graphql")
      .header("User-Agent", userAgent)
      .header("Content-Type", "application/json")
      .body(slug.toGetArtistQuery())

    val response = client(request)

    return when (response.status) {
      OK   -> raArtistResponse(response).data.artist
      else -> null
    }
  }

  //TODO: move to result type
  fun getEventsFor(
    raArtist: RAArtist,
    startDate: LocalDate
  ): List<RAEventsResponse.RADataWrapper.RAListing.RAEvent> {
    val request = Request(POST, "/graphql")
      .header("User-Agent", userAgent)
      .header("Content-Type", "application/json")
      .body(raArtist.toGetEventsQuery(startDate))

    val response = client(request)

    return when (response.status) {
      OK   -> raEventsResponse(response).data.listing.data
      else -> listOf()
    }
  }
}
