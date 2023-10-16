package de.p10r.domain.models

import java.util.*

data class ArtistId(val id: String) {
  companion object {
    fun new() = ArtistId("art-${UUID.randomUUID()}")

    fun of(id: String) = ArtistId(id)
  }
}
