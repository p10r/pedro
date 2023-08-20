package de.p10r

import de.p10r.ra.RAClient
import dev.forkhandles.result4k.orThrow

class ArtistsRegistry(
  private val repository: ArtistRepository,
  private val raClient: RAClient
) {
  fun list(): List<Artist> =
    repository.findAll().orThrow()

  fun add(inputUrl: InputUrl) { //TODO handle errors
    val artist = raClient.getArtistBy(inputUrl.toRASlug()) ?: return
    if (repository.findByName(artist.name).orThrow() != null) return
    repository.create(NewArtist(artist.name))
  }
}
