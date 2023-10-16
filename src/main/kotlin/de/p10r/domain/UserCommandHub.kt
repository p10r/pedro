package de.p10r.domain

import de.p10r.UserId

class UserCommandHub(val artistsRegistry: ArtistsRegistry) {
  fun process(command: UserCommand): UserCommandResult = when (command) {
    is UserCommand.FollowArtist -> {
      artistsRegistry.follow(command.userId, command.artist)
      UserCommandResult.AddedArtist
    }

    is UserCommand.ListArtists -> {
      UserCommandResult.Artists(artistsRegistry.list())
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
