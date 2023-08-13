package de.p10r

import de.p10r.fakes.RAServer
import de.p10r.fakes.new
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.events.Events
import org.http4k.server.SunHttp
import org.http4k.server.asServer
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
  raServer: HttpHandler = RAServer(
    mapOf(
      RASlug("boysnoize") to RAArtistResponse(
        RAArtistResponse.RAData(
          RAArtist("943", "Boys Noize")
        )
      )
    )
  ),
  events: Events = {}
): HttpHandler {
  val db = Database.new(existingArtists)

  return App(
    database = db,
    raUri = raUri,
    raHttp = raServer,
    events = loggingEvents() then events,
    features = Features(onlyPing = false)
  )
}
