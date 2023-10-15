package de.p10r.adapters.driving

import de.p10r.UserId
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driving.IncomingTelegramRequest.Message
import de.p10r.domain.Artist
import de.p10r.domain.TelegramCommandResult
import de.p10r.infrastructure.UncaughtExceptionEvent
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.events.Event
import org.http4k.events.HttpEvent
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class ApiTests {
  @Test
  fun `reports every incoming http tx`() {
    val recordedEvents = mutableListOf<Event>()
    val api = Api(recordedEvents)

    api(Request(GET, "/artists"))
    api(Request(GET, "/"))

    expectThat(recordedEvents.filterIsInstance<HttpEvent.Incoming>()).hasSize(2)
  }

  @Test
  fun `reports uncaught exceptions`() {
    val recordedEvents = mutableListOf<Event>()
    val api = Api(recordedEvents, { throw RuntimeException("boom") })
    api(Request(GET, "/artists"))

    expectThat(recordedEvents.filterIsInstance<UncaughtExceptionEvent>()).hasSize(1)
  }

  @Test
  fun `Telegram security is active`() {
    val app = Api(users = listOf(UserId(1)))
    expectThat(app.postTelegramMessage(from = Message.From(id = 2)).status)
      .isEqualTo(UNAUTHORIZED)
  }

  private fun Api(
    recordedEvents: MutableList<Event> = mutableListOf(),
    listAllArtists: () -> List<Artist> = { emptyList() },
    users: List<UserId> = listOf(UserId(1)),
  ) = ApiRoutes(
    processTelegramCommands = { TelegramCommandResult.AddedArtist },
    listAllArtists = listAllArtists,
    secret = TelegramConfig.IncomingTelegramRequestSecret("w/e"),
    users = users,
    sendMessage = { _, _ -> Response(Status.OK) },
    events = recordedEvents::add
  )
}
