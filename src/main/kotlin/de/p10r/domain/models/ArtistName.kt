package de.p10r.domain.models

data class ArtistName private constructor(val value: String) {
  companion object {
    fun of(input: String): ArtistName {
      val baseUrl = "http://ra.co/dj/"
      val sanitized =
        if (input.startsWith(baseUrl)) input.removePrefix(baseUrl)
        else input

      return ArtistName(sanitized)
    }
  }
}
