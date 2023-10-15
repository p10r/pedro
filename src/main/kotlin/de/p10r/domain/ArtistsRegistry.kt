package de.p10r.domain

import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.ra.RAClient

class ArtistsRegistry(
  private val repository: ArtistRepository,
  private val raClient: RAClient
) {
  fun list(): List<Artist> = repository.findAll()

  fun add(inputUrl: InputUrl) {
    val artist = raClient.getArtistBy(inputUrl.toRASlug()) ?: return
    if (repository.findByName(artist.name) != null) return
    repository.create(NewArtist(artist.name))
  }
}
