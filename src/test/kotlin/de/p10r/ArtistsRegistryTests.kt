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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
    val repository = SqliteArtistRepository(Database.new(existingArtists))
    val artistsRegistry = ArtistsRegistry(repository, raClient)
    assertEquals(existingArtists, artistsRegistry.list().toNewArtists())
  }

  @Test
  fun `adds artist from resident advisor`() {
    val repository = SqliteArtistRepository(Database.new())
    val artistsRegistry = ArtistsRegistry(repository, raClient)

    artistsRegistry.add(InputUrl.unsafe("https://ra.co/dj/justice"))

    assertTrue(artistsRegistry.list().contains(Artist(ArtistId.unsafe(1), "Justice")))
  }

  @Test
  fun `doesn't add artist if already existing`() {
    val repository = SqliteArtistRepository(Database.new())
    val artistsRegistry = ArtistsRegistry(repository, raClient)

    assertEquals(0, artistsRegistry.list().size)

    artistsRegistry.add(InputUrl.unsafe("https://ra.co/dj/justice"))
    artistsRegistry.add(InputUrl.unsafe("https://ra.co/dj/justice"))

    assertEquals(1, artistsRegistry.list().size)
  }
}
