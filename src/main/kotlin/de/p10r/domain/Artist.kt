package de.p10r.domain

import java.util.*

data class Artist(
  val id: ArtistId,
  val name: String,
) {
  companion object {
    fun of(newArtist: NewArtist) = Artist(ArtistId.new(), newArtist.name)
  }
}

data class NewArtist(
  val name: String
)

data class ArtistId(val id: String) {
  companion object {
    fun new() = ArtistId("art-${UUID.randomUUID()}")

    fun of(id: String) = ArtistId(id)
  }
}
