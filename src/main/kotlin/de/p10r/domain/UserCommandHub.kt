package de.p10r.domain

import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.ra.RASlug
import de.p10r.domain.models.NewArtist
import de.p10r.domain.models.UserCommand
import de.p10r.domain.models.UserCommand.FollowArtist
import de.p10r.domain.models.UserCommand.ListArtists
import de.p10r.domain.models.UserCommandResult

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

