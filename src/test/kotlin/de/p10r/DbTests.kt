package de.p10r

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.forkhandles.values.ofOrNull
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder

fun Database.Companion.new() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).let {
  Schema.create(it)
  Database(it)
}

fun ArtistId.Companion.unsafe(id: Long) = ofOrNull(id)!!


class DbTests {
  val db = Database.new()
  val store = ArtistsStore(db)
  val sql = db.artistQueries

  @Test
  fun `find all stored artists`() {
    sql.create("Boys Noize").executeAsOne()
    sql.create("Daft Punk").executeAsOne()

    expectThat(store.findAll()).containsExactlyInAnyOrder(
      Artist(ArtistId.unsafe(1), "Boys Noize"),
      Artist(ArtistId.unsafe(2), "Daft Punk")
    )
  }

  @Test
  fun `insert a new artist`() {
    store.create(NewArtist("Me"))

    expectThat(store.findAll())
      .containsExactlyInAnyOrder(Artist(ArtistId.unsafe(1), "Me"))
  }
}
