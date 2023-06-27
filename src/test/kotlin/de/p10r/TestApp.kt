package de.p10r

import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.template.HandlebarsTemplates
import kotlin.system.measureTimeMillis

fun main() {
  measureTimeMillis {
    TestApp().asServer(SunHttp(8080)).start()
  }.let { println("Server started in $it ms") }
}

fun TestApp(
  existingArtists: List<NewArtist> = listOf(
    NewArtist("Boys Noize"),
    NewArtist("Justin Tinderdate"),
    NewArtist("Sinamin"),
  ),
  raUri: Uri = Uri.of("http://ra.co"),
  raServer: HttpHandler = FakeRAServer(
    mapOf(
      RASlug("boysnoize") to RAArtistResponse(
        RAArtistResponse.RAData(
          RAArtist("943", "Boys Noize")
        )
      )
    )
  )
): HttpHandler {
  val raClient = RAClient(raUri, raServer)
  val repository = ArtistRepository(Database.new(existingArtists))
  val artistsRegistry = ArtistsRegistry(repository, raClient)
  val renderer = HandlebarsTemplates().HotReload("src/main/resources")

  return DebuggingFilters.PrintRequest()
    .then(App(artistsRegistry, renderer))
}
