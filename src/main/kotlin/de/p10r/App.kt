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
import org.http4k.filter.ServerFilters
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.string
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.TemplateRenderer
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
  val repository = ArtistRepository(db)
  val client = OkHttpClient.Builder()
    .followRedirects(true)
    .build()
    .let { OkHttp(it) }
  val raClient = RAClient(Uri.of("https://ra.co"), client)
  val artistsRegistry = ArtistsRegistry(repository, raClient)

  return ServerFilters.CatchAll()
    .then(App(artistsRegistry, HandlebarsTemplates().Caching("src/main/resources")))
}

private val inputUrl = FormField.string().required("url")
private val idFrom = Body.webForm(Validator.Feedback, inputUrl).toLens()

fun App(
  artistsRegistry: ArtistsRegistry,
  renderer: TemplateRenderer
): HttpHandler = routes(
  "/" bind GET to {
    val view = IndexViewModel(artistsRegistry.list())
    Response(OK).body(renderer(view))
  },
  "/artists" bind POST to { req ->
    idFrom(req).let(inputUrl)
      .let(InputUrl::ofOrNull)
      ?.let {
        artistsRegistry.add(it)
        Response(Status.SEE_OTHER).header("Location", "/")
      } ?: Response(Status.BAD_REQUEST)
  }
)

private fun Database.checkConnection() = artistQueries.selectAll()
