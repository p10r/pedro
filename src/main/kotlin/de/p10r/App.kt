package de.p10r

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import de.p10r.Database.Companion.Schema
import okhttp3.OkHttpClient
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.events.Events
import org.http4k.filter.ServerFilters
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.system.measureTimeMillis


fun main() {
  measureTimeMillis {
    ProdApp().asServer(SunHttp(8080)).start()
  }.let { println("Server started in $it ms") }
}

//TODO check connection
fun ProdApp(): HttpHandler {
  val driver = JdbcSqliteDriver("jdbc:sqlite:src/main/resources/pedro-local.db")
  val db = Database(driver).apply { Schema.create(driver) }
  val client = OkHttpClient.Builder()
    .followRedirects(true)
    .build()
    .let { OkHttp(it) }

  return App(
    database = db,
    Uri.of("https://ra.co"),
    client,
    events = {},
    Features(onlyPing = true)
  )
}

private val inputUrl = FormField.map { InputUrl.ofOrNull(it) }.required("url")
private val idFrom = Body.webForm(Validator.Feedback, inputUrl).toLens()

fun App(
  database: Database,
  raUri: Uri,
  raHttp: HttpHandler,
  events: Events,
  features: Features
): HttpHandler {
  val artistRepository = ArtistRepository(database)
  val raClient = RAClient(raUri, AppOutgoingHttp(events, raHttp))
  val artistsRegistry = ArtistsRegistry(artistRepository, raClient)

  return AppIncomingHttp(
    events,
    ServerFilters.CatchAll {
      println(it)
      events(UncaughtExceptionEvent(it))
      Response(Status.INTERNAL_SERVER_ERROR)
    }.then(AppRoutes(artistsRegistry))
  )
}

private fun AppRoutes(artistsRegistry: ArtistsRegistry) = routes(
  "/artists" bind POST to { req ->
    idFrom(req).let(inputUrl)?.let {
      artistsRegistry.add(it)
      Response(Status.SEE_OTHER).header("Location", "/")
    } ?: Response(Status.BAD_REQUEST)
  },
  "/ping" bind GET to { Response(OK).body("pong") }
)

