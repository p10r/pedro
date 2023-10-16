package de.p10r.domain.models

sealed interface UserCommand {
  data class FollowArtist(val userId: UserId, val artist: ArtistName) : UserCommand
  data class ListArtists(val userId: UserId) : UserCommand
}
