package de.p10r

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.forkhandles.result4k.Success
import dev.forkhandles.values.ofOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

fun Database.Companion.new() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).let {
  Schema.create(it)
  Database(it)
}

fun ArtistId.Companion.unsafe(id: Long) = ofOrNull(id)!!


class DatabaseTests {
  val db = Database.new()
  val store = ArtistsStore(db)
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
      store.findAll()
    )
  }

  @Test
  fun `insert a new artist`() {
    store.create(NewArtist("Me"))

    assertEquals(
      Success(listOf(Artist(ArtistId.unsafe(1), "Me"))),
      store.findAll()
    )
  }
}
