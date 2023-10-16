package de.p10r.domain

import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.ra.RAArtist
import de.p10r.adapters.driven.ra.RAArtistResponse
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.ra.RASlug
import de.p10r.containsExactlyInAnyOrderIgnoringIds
import de.p10r.domain.UserCommand.FollowArtist
import de.p10r.domain.UserCommand.ListArtists
import de.p10r.fixtures.DomainTelegramUser
import de.p10r.fixtures.FakeRAServer
import de.p10r.fixtures.new
import de.p10r.isArtistsResult
import de.p10r.scenarios.TelegramUserScenarios
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty

class HubTests : TelegramUserScenarios {
  val raClient = RAClient(
    Uri.of("http://ra.co"),
    FakeRAServer(
      mapOf(
        RASlug("boysnoize") to RAArtistResponse(
          RAArtistResponse.RAData(
            RAArtist("943", "Boys Noize")
          )
        ),
        RASlug("sabura") to RAArtistResponse(
          RAArtistResponse.RAData(
            RAArtist("2", "Sabura")
          )
        ),
        RASlug("justice") to RAArtistResponse(
          RAArtistResponse.RAData(
            RAArtist("3", "Justice")
          )
        )
      )
    )
  )
  val repository = ArtistRepository.new()
  val hub = UserCommandHub(repository, raClient)

  override val sarah = DomainTelegramUser(hub, UserId(777))
  override val joe = DomainTelegramUser(hub, UserId(666))

  @Test
  fun `lists existing artists`() {
    val sarah = UserId(1)
    val existingArtists = listOf(
      NewArtist("Boys Noize"),
      NewArtist("Justin Tinderdate"),
      NewArtist("Sinamin"),
    ).onEach { repository.save(it, sarah) }

    expectThat(hub.process(ListArtists(sarah)))
      .isArtistsResult()
      .containsExactlyInAnyOrderIgnoringIds(existingArtists.map { Artist.of(it) })
  }

  @Test
  fun `imports artist form RA with user as follower`() {
    hub.process(FollowArtist(UserId(1), ArtistName.of("justice")))

    expectThat(hub.process(ListArtists(UserId(1))))
      .isArtistsResult()
      .containsExactlyInAnyOrderIgnoringIds(listOf(Artist(ArtistId.new(), "Justice")))
  }

  @Test
  fun `doesn't add new artist if already existing`() {
    val repository = ArtistRepository.new()
    val hub = UserCommandHub(repository, raClient)

    expectThat(repository.findAll()).isEmpty()

    expectThat(hub.process(ListArtists(UserId(1))))
      .isArtistsResult()
      .isEmpty()

    hub.process(FollowArtist(UserId(1), ArtistName.of("justice")))
    hub.process(FollowArtist(UserId(1), ArtistName.of("justice")))

    expectThat(hub.process(ListArtists(UserId(1))))
      .isArtistsResult()
      .containsExactlyInAnyOrderIgnoringIds(listOf(Artist(ArtistId.new(), "Justice")))
  }

}
