package de.p10r.domain

import de.p10r.UserId

class TelegramProcessor(val artistsRegistry: ArtistsRegistry) {
  fun process(command: TelegramCommand): TelegramCommandResult = when (command) {
    is TelegramCommand.AddArtist   -> {
      artistsRegistry.follow(command.userId, command.artist)
      TelegramCommandResult.AddedArtist
    }

    is TelegramCommand.ListArtists -> {
      TelegramCommandResult.Artists(artistsRegistry.list())
    }
  }
}

sealed interface TelegramCommand {
  data class AddArtist(val userId: UserId, val artist: String) : TelegramCommand
  object ListArtists : TelegramCommand
}

sealed interface TelegramCommandResult {
  object AddedArtist : TelegramCommandResult
  data class Artists(val artists: List<Artist>) : TelegramCommandResult
}
