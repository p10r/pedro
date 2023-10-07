package de.p10r.fixtures

import de.p10r.ra.RAArtistResponse
import de.p10r.ra.RASlug
import de.p10r.ra.raArtistResponse
import de.p10r.ra.toGetArtistQuery
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.net.URL

fun FakeRAServer(
  artistBySlugId: Map<RASlug, RAArtistResponse>
): RoutingHttpHandler = DebuggingFilters.PrintRequest().then(
  routes(
    "/graphql" bind POST to { request ->
      if (request.isGetArtistRequest(artistBySlugId))
        getArtistResponse(artistBySlugId, request)
      else
        getEventsForArtistResponse()
    }
  )
)

private fun Request.isGetArtistRequest(artistBySlugId: Map<RASlug, RAArtistResponse>): Boolean =
  artistBySlugId.keys.find { it.toGetArtistQuery() == bodyString() } != null

private fun getArtistResponse(
  artistBySlugId: Map<RASlug, RAArtistResponse>,
  request: Request
): Response {
  val matchingSlug =
    artistBySlugId.keys.find { it.toGetArtistQuery() == request.bodyString() }
  val artist =
    if (matchingSlug == null) null
    else artistBySlugId[matchingSlug]

  return if (artist == null) {
    Response(OK).header("content-type", "application/json")
      .body(url("get-artist-by-slug.json").readText())
  } else {
    Response(OK).with(raArtistResponse of artist)
  }
}

private fun getEventsForArtistResponse(): Response =
  Response(OK).header("content-type", "application/json")
    .body(url("get-events-for-boys-noize.json").readText())

@Suppress("SameParameterValue")
private fun url(path: String): URL = object {}::class.java.classLoader.getResource(path)!!
