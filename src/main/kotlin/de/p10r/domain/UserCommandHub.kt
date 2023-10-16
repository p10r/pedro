package de.p10r.domain

import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.ra.RASlug
import de.p10r.domain.UserCommand.FollowArtist
import de.p10r.domain.UserCommand.ListArtists

class UserCommandHub(
  val repository: ArtistRepository,
  val raClient: RAClient
) {
  fun process(command: UserCommand): UserCommandResult {
    return when (command) {
      is FollowArtist -> {
        val result = raClient.getArtistBy(RASlug(command.artist.value))
          ?: return UserCommandResult.AddedArtist

        repository.save(NewArtist(result.name), command.userId)
        UserCommandResult.AddedArtist
      }

      is ListArtists  -> {
        UserCommandResult.Artists(repository.findAllBy(command.userId))
      }
    }
  }
}

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

sealed interface UserCommand {
  data class FollowArtist(val userId: UserId, val artist: ArtistName) : UserCommand
  data class ListArtists(val userId: UserId) : UserCommand
}

sealed interface UserCommandResult {
  object AddedArtist : UserCommandResult
  data class Artists(val artists: List<Artist>) : UserCommandResult
}
