package de.p10r

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

fun Database.Companion.new(
  existingArtists: List<NewArtist> = emptyList()
) = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).let {
  Schema.create(it)
  Database(it)
}.also { db ->
  existingArtists.forEach {
    val id = db.artistQueries.create(it.name).executeAsOne()
    println("Created $id ${it.name}")
  }
}


class ArtistRepositoryTests {
  val db = Database.new()
  val repository = ArtistRepository(db)
  val sql = db.artistQueries

  @Test
  fun `find all stored artists`() {
    sql.create("Boys Noize").executeAsOne()
    sql.create("Daft Punk").executeAsOne()

    assertEquals(
      Success(
        listOf(
          Artist(ArtistId.unsafe(1), "Boys Noize"),
          Artist(ArtistId.unsafe(2), "Daft Punk")
        )
      ),
      repository.findAll()
    )
  }

  @Test
  fun `insert a new artist`() {
    repository.create(NewArtist("Me"))

    assertEquals(
      Success(listOf(Artist(ArtistId.unsafe(1), "Me"))),
      repository.findAll()
    )
  }

  @Test
  fun `find artist by name`() {
    with(repository) {
      create(NewArtist("Me"))
      create(NewArtist("You"))
      create(NewArtist("Them"))
      create(NewArtist("sOmEoNe ElSe"))

      assertEquals(
        Success(Artist(ArtistId.unsafe(2), "You")),
        findByName("You")
      )

      assertEquals(
        Success(Artist(ArtistId.unsafe(4), "sOmEoNe ElSe")),
        findByName("someone else")
      )
    }
  }

  @Test
  fun `returns null when artist can't be found`() {
    assertEquals(
      Success(null),
      repository.findByName("someone else")
    )
  }
}
