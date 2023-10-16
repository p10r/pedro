package de.p10r.adapters.driven.db

import de.p10r.containsExactlyInAnyOrderIgnoringIds
import de.p10r.domain.models.Artist
import de.p10r.domain.models.ArtistId
import de.p10r.domain.models.NewArtist
import de.p10r.domain.models.UserId
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
    repository.save(NewArtist("Boys Noize"))
    repository.save(NewArtist("Daft Punk"))

    val stored = repository.findAll().map { it.name }

    expectThat(stored).hasSize(2)
    expectThat(stored).contains("Boys Noize", "Daft Punk")
  }

  @Test
  fun `insert a new artist`() {
    repository.save(NewArtist("Me"))

    val stored = repository.findAll()

    expectThat(stored).hasSize(1)
    expectThat(stored.first()).isEqualToIgnoringId(Artist(ArtistId.of("w/e"), "Me"))
  }

  @Test
  fun `find artist by name`() {
    with(repository) {
      save(NewArtist("Me"))
      save(NewArtist("You"))
      save(NewArtist("Them"))
      save(NewArtist("sOmEoNe ElSe"))

      expectThat(findByName("You")).isNotNull()
        .isEqualToIgnoringId(Artist(ArtistId.of("w/e"), "You"))

      expectThat(findByName("someone else")).isNotNull()
        .isEqualToIgnoringId(Artist(ArtistId.of("w/e"), "sOmEoNe ElSe"))
    }
  }

  @Test
  fun `find artist by user id`() {
    val joe = UserId(1)
    val sarah = UserId(2)
    with(repository) {
      val boysNoize = NewArtist("Boys Noize")
      val disclosure = NewArtist("Disclosure")
      val sabura = NewArtist("Sabura")
      val erobique = NewArtist("erobique")

      save(boysNoize, joe)
      save(boysNoize, sarah)
      save(disclosure, sarah)
      save(sabura, joe)
      save(erobique, joe)

      expectThat(findAllBy(joe)).containsExactlyInAnyOrderIgnoringIds(
        listOf(
          Artist.of(boysNoize),
          Artist.of(sabura),
          Artist.of(erobique)
        )
      )

      expectThat(findAllBy(sarah)).containsExactlyInAnyOrderIgnoringIds(
        listOf(
          Artist.of(boysNoize),
          Artist.of(disclosure),
        )
      )
    }
  }

  @Test
  fun `returns null when artist can't be found`() {
    expectThat(repository.findByName("someone else")).isNull()
  }
}
