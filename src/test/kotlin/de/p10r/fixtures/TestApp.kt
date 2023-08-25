package de.p10r.fixtures

import de.p10r.App
import de.p10r.Database
import de.p10r.Features
import de.p10r.NewArtist
import de.p10r.TelegramSecret
import de.p10r.UserId
import de.p10r.loggingEvents
import de.p10r.ra.RAArtist
import de.p10r.ra.RAArtistResponse
import de.p10r.ra.RASlug
import de.p10r.then
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
  secret: TelegramSecret = TelegramSecret("secret"),
  users: List<UserId> = listOf(UserId(1)),
  events: Events = {},
  db: Database = Database.new(existingArtists)
): HttpHandler {

  return App(
    database = db,
    raUri = raUri,
    raHttp = raServer,
    events = loggingEvents() then events,
    secret,
    users,
    features = Features(onlyPing = false),
  )
}
