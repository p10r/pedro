package de.p10r

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArtistsRegistryTest {
  val existingArtists = listOf(
    NewArtist("Boys Noize"),
    NewArtist("Justin Tinderdate"),
    NewArtist("Sinamin"),
  )
  val repository = ArtistRepository(Database.new(existingArtists))
  val artistsRegistry = ArtistsRegistry(repository)

  @Test
  fun `lists existing artists`() {
    assertEquals(existingArtists, artistsRegistry.list().toNewArtists())
  }
}
