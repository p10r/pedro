package de.p10r

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.format.Jackson.auto

val raArtistResponse = Body.auto<RAArtistResponse>().toLens()

fun RASlug.toGetArtistQuery() =
  """{"query":"{\n    artist(slug:\"$value\") {\n id\n name\n }\n}\n","variables":{}}"""

class RAClient(
  raUri: Uri,
  http: HttpHandler
) {

  private val client = ClientFilters.SetBaseUriFrom(raUri).then(http)
  fun getArtistBy(slug: RASlug): RAArtist? {
    //The header is needed to actually make it work
    val request = Request(Method.POST, "/graphql")
      .header("Host", "ra.co")
      .header(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
          " AppleWebKit/537.36 (KHTML, like Gecko)" +
          " Chrome/113.0.0.0 Safari/537.36"
      )
      .header("Content-Type", "application/json")
      .body(slug.toGetArtistQuery())

    val response = client(request)

    return when (response.status) {
      OK   -> raArtistResponse(response).data.artist
      else -> null
    }
  }
}
