package de.p10r.domain

import de.p10r.UserId
import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.ra.RASlug

class ArtistsRegistry(
  private val repository: ArtistRepository,
  private val raClient: RAClient
) {
  fun list(): List<Artist> = repository.findAll()

  fun follow(userId: UserId, artist: String) {
    val result = raClient.getArtistBy(RASlug(artist)) ?: return

    if (repository.findByName(result.name) != null) return
    repository.create(NewArtist(result.name))
  }
}
