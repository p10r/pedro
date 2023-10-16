package de.p10r.domain

import de.p10r.UserId
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
        val result = raClient.getArtistBy(RASlug(command.artist))
          ?: return UserCommandResult.AddedArtist

        if (repository.findByName(result.name) != null)
          return UserCommandResult.AddedArtist

        repository.create(NewArtist(result.name))

        UserCommandResult.AddedArtist
      }

      is ListArtists  -> {
        UserCommandResult.Artists(repository.findAll())
      }
    }
  }
}

sealed interface UserCommand {
  data class FollowArtist(val userId: UserId, val artist: String) : UserCommand
  object ListArtists : UserCommand
}

sealed interface UserCommandResult {
  object AddedArtist : UserCommandResult
  data class Artists(val artists: List<Artist>) : UserCommandResult
}
