package de.p10r

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.resultFrom
import dev.forkhandles.values.ofOrNull

class ArtistsStore(database: Database) {
  private val sql = database.artistQueries

  fun findAll(): Result4k<List<Artist>, Exception> =
    resultFrom { sql.selectAll().executeAsList().toArtists() }

  fun create(newArtist: NewArtist): Result<Artist, Exception> =
    resultFrom { sql.create(newArtist.name).executeAsOne().toArtist() }

  private fun Stored_artists.toArtist() = Artist(ArtistId.ofOrNull(id)!!, name)

  private fun List<Stored_artists>.toArtists() = map { it.toArtist() }
}
