package de.p10r.domain.models

sealed interface UserCommandResult {
  object AddedArtist : UserCommandResult
  data class Artists(val artists: List<Artist>) : UserCommandResult
}
