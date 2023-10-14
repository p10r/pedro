package de.p10r

import de.p10r.fixtures.new
import de.p10r.ra.RAArtist
import de.p10r.ra.RAArtistResponse
import de.p10r.ra.RAClient
import de.p10r.ra.raArtistResponse
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.with
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains

class ArtistsRegistryTests {
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
  fun `adds artist from resident advisor`() {
    val repository = ArtistRepository.new()
    val artistsRegistry = ArtistsRegistry(repository, raClient)

    artistsRegistry.add(inputUrlOf("https://ra.co/dj/justice"))

    expectThat(artistsRegistry.list().toNewArtists()).contains(NewArtist("Justice"))
  }

  @Test
  fun `doesn't add artist if already existing`() {
    val repository = ArtistRepository.new()
    val artistsRegistry = ArtistsRegistry(repository, raClient)

    assertEquals(0, artistsRegistry.list().size)

    artistsRegistry.add(inputUrlOf("https://ra.co/dj/justice"))
    artistsRegistry.add(inputUrlOf("https://ra.co/dj/justice"))

    assertEquals(1, artistsRegistry.list().size)
  }
}
