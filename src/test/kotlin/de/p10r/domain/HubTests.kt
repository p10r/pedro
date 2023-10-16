package de.p10r.domain

import de.p10r.UserId
import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.ra.RAArtist
import de.p10r.adapters.driven.ra.RAArtistResponse
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.ra.raArtistResponse
import de.p10r.containsExactlyInAnyOrderIgnoringIds
import de.p10r.domain.UserCommand.FollowArtist
import de.p10r.domain.UserCommand.ListArtists
import de.p10r.fixtures.new
import de.p10r.isArtistsResult
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.with
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty

class HubTests {
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

    val hub = UserCommandHub(ArtistRepository.new(existingArtists), raClient)

    expectThat(hub.process(ListArtists))
      .isArtistsResult()
      .containsExactlyInAnyOrderIgnoringIds(existingArtists.map { Artist.of(it) })
  }

  @Test
  fun `follows artist from resident advisor with user as follower`() {
    val hub = UserCommandHub(ArtistRepository.new(), raClient)

    hub.process(FollowArtist(UserId(1), "justice"))

    expectThat(hub.process(ListArtists))
      .isArtistsResult()
      .containsExactlyInAnyOrderIgnoringIds(listOf(Artist(ArtistId.new(), "Justice")))
  }

  @Test
  fun `doesn't follow artist if already existing`() {
    val repository = ArtistRepository.new()
    val hub = UserCommandHub(repository, raClient)

    assertEquals(0, repository.findAll().size)

    expectThat(hub.process(ListArtists))
      .isArtistsResult()
      .isEmpty()

    hub.process(FollowArtist(UserId(1), "justice"))
    hub.process(FollowArtist(UserId(1), "justice"))

    expectThat(hub.process(ListArtists))
      .isArtistsResult()
      .containsExactlyInAnyOrderIgnoringIds(listOf(Artist(ArtistId.new(), "Justice")))
  }
}
