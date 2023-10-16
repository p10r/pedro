package de.p10r.domain

import de.p10r.UserId
import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.ra.RAArtist
import de.p10r.adapters.driven.ra.RAArtistResponse
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.ra.raArtistResponse
import de.p10r.fixtures.new
import de.p10r.toNewArtists
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.with
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains

class IncomingCommandsTests {
  val raClient = RAClient(
    raUri = Uri.of("http://resident-advisor"),
    http = { _: Request ->
      Response(Status.OK).with(
        raArtistResponse of RAArtistResponse(
          RAArtistResponse.RAData(
            RAArtist("777", "Justice")
          )
        )
      )
    }
  )

  @Test
  fun `lists existing artists`() {
    val existingArtists = listOf(
      NewArtist("Boys Noize"),
      NewArtist("Justin Tinderdate"),
      NewArtist("Sinamin"),
    )
    val repository = ArtistRepository.new(existingArtists)
    val artistsRegistry = ArtistsRegistry(repository, raClient)

    expectThat(artistsRegistry.list().toNewArtists()).contains(existingArtists)
  }

  @Test
  fun `follows artist from resident advisor with user as follower`() {
    val repository = ArtistRepository.new()
    val artistsRegistry = ArtistsRegistry(repository, raClient)

    artistsRegistry.follow(UserId(1), "justice")

    expectThat(artistsRegistry.list().toNewArtists()).contains(NewArtist("Justice"))
  }

  @Test
  fun `doesn't follow artist if already existing`() {
    val repository = ArtistRepository.new()
    val artistsRegistry = ArtistsRegistry(repository, raClient)

    assertEquals(0, artistsRegistry.list().size)

    artistsRegistry.follow(UserId(1), "https://ra.co/dj/justice")
    artistsRegistry.follow(UserId(1), "https://ra.co/dj/justice")

    assertEquals(1, artistsRegistry.list().size)
  }
}
