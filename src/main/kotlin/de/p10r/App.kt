package de.p10r

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer


fun main() {

}

fun App(
  artistsRegistry: ArtistsRegistry,
  renderer: TemplateRenderer
): HttpHandler = routes(
  "/" bind GET to {
    val view = IndexViewModel(artistsRegistry.list())
    Response(OK).body(renderer(view))
  }
)
