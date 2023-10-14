package de.p10r

import strikt.api.Assertion
import strikt.java.propertiesAreEqualToIgnoring

fun inputUrlOf(value: String) = InputUrl.ofOrNull(value)!!

fun List<Artist>.toNewArtists() = map { it.toNewArtist() }
fun Artist.toNewArtist() = NewArtist(name)

fun readTextFrom(path: String) = {}::class.java.classLoader.getResource(path)!!.readText()

fun Assertion.Builder<Artist>.isEqualToIgnoringId(other: Artist) =
  this.propertiesAreEqualToIgnoring(other, Artist::id)

