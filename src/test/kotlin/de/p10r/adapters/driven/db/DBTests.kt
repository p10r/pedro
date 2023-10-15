package de.p10r.adapters.driven.db

import de.p10r.domain.Artist
import de.p10r.domain.ArtistId
import de.p10r.domain.NewArtist
import de.p10r.fixtures.new
import de.p10r.isEqualToIgnoringId
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class DBTests {
  val repository = ArtistRepository.new()

  @Test
  fun `find all stored artists`() {
    repository.create(NewArtist("Boys Noize"))
    repository.create(NewArtist("Daft Punk"))

    val stored = repository.findAll().map { it.name }

    expectThat(stored).hasSize(2)
    expectThat(stored).contains("Boys Noize", "Daft Punk")
  }

  @Test
  fun `insert a new artist`() {
    repository.create(NewArtist("Me"))

    val stored = repository.findAll()

    expectThat(stored).hasSize(1)
    expectThat(stored.first()).isEqualToIgnoringId(Artist(ArtistId.of("w/e"), "Me"))
  }

  @Test
  fun `find artist by name`() {
    with(repository) {
      create(NewArtist("Me"))
      create(NewArtist("You"))
      create(NewArtist("Them"))
      create(NewArtist("sOmEoNe ElSe"))

      expectThat(findByName("You")).isNotNull()
        .isEqualToIgnoringId(Artist(ArtistId.of("w/e"), "You"))

      expectThat(findByName("someone else")).isNotNull()
        .isEqualToIgnoringId(Artist(ArtistId.of("w/e"), "sOmEoNe ElSe"))
    }
  }

  @Test
  fun `returns null when artist can't be found`() {
    expectThat(repository.findByName("someone else")).isNull()
  }
}
