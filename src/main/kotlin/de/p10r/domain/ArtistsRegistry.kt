package de.p10r.domain

import de.p10r.adapters.driven.db.ArtistRepository

class ArtistsRegistry(private val repository: ArtistRepository) {
  fun listAllFor(userId: UserId): List<Artist> = repository.findAllBy(userId)

  fun list() = repository.findAll()
}
