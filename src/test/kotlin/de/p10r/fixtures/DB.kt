package de.p10r.fixtures

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import de.p10r.Database
import de.p10r.NewArtist

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
