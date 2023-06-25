package de.p10r

import dev.forkhandles.values.LongValue
import dev.forkhandles.values.LongValueFactory
import dev.forkhandles.values.minValue

data class Artist(
  val id: ArtistId,
  val name: String,
)

class ArtistId private constructor(id: Long) : LongValue(id) {
  val id = "art-$id"

  companion object : LongValueFactory<ArtistId>(::ArtistId, 1.toLong().minValue)
}
