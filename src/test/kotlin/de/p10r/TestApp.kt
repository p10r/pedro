package de.p10r

import org.http4k.core.HttpHandler
import org.http4k.template.HandlebarsTemplates

fun main() {

}

fun TestApp(): HttpHandler {
  val existingArtists = listOf(
    NewArtist("Boys Noize"),
    NewArtist("Justin Tinderdate"),
    NewArtist("Sinamin"),
  )
  val repository = ArtistRepository(Database.new(existingArtists))
  val artistsRegistry = ArtistsRegistry(repository)
  val renderer = HandlebarsTemplates().HotReload("src/main/resources")

  return App(artistsRegistry, renderer)
}
