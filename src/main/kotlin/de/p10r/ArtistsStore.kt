package de.p10r

import dev.forkhandles.values.ofOrNull

class ArtistsStore(database: Database) {
  private val sql = database.artistQueries

  fun findAll(): List<Artist> =
    sql.selectAll().executeAsList().toArtists()

  fun create(newArtist: NewArtist): Artist =
    sql.create(newArtist.name).executeAsOne().toArtist()

  private fun Stored_artists.toArtist() = Artist(ArtistId.ofOrNull(id)!!, name)

  private fun List<Stored_artists>.toArtists() = map { it.toArtist() }
}
