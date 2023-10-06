package de.p10r

import de.p10r.ra.RAClient

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
