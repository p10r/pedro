package de.p10r

import dev.forkhandles.result4k.orThrow

class ArtistsRegistry(private val repository: ArtistRepository) {
  fun list(): List<Artist> =
    repository.findAll().orThrow()
}
