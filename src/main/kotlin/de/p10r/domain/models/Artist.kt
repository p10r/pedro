package de.p10r.domain.models

data class Artist(
  val id: ArtistId,
  val name: String,
) {
  companion object {
    fun of(newArtist: NewArtist) = Artist(ArtistId.new(), newArtist.name)
  }
}
