package de.p10r

import dev.forkhandles.values.ofOrNull

fun ArtistId.Companion.unsafe(id: Long) = ofOrNull(id)!!

fun List<Artist>.toNewArtists() = map { it.toNewArtist() }
fun Artist.toNewArtist() = NewArtist(name)
