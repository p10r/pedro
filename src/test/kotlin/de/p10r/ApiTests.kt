package de.p10r

import de.p10r.fixtures.TestApp
import de.p10r.infrastructure.UncaughtExceptionEvent
import de.p10r.telegram.IncomingTelegramRequest.Message
import de.p10r.telegram.postTelegramMessage
import org.http4k.core.ContentType
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.body.form
import org.http4k.events.Event
import org.http4k.events.HttpEvent
import org.http4k.lens.Header
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class ApiTests {
  @Test
  fun `reports every incoming http tx`() {
    val recordedEvents = mutableListOf<Event>()
    val app = TestApp(events = recordedEvents::add, users = listOf(UserId(1)))

    app(Request(GET, "/artists"))
    app(Request(GET, "/"))
    app(Request(POST, "/artists"))

    expectThat(recordedEvents.filterIsInstance<HttpEvent.Incoming>()).hasSize(3)
  }

  @Test
  fun `reports outgoing http tx`() {
    val recordedEvents = mutableListOf<Event>()
    val app = TestApp(events = recordedEvents::add, users = listOf(UserId(1)))

    app(
      Request(POST, "/artists").header(
        Header.CONTENT_TYPE.meta.name,
        ContentType.APPLICATION_FORM_URLENCODED.toHeaderValue()
      ).form("url", "http://ra.com/dj/someone")
    )

    expectThat(recordedEvents.filterIsInstance<HttpEvent.Incoming>()).hasSize(1)
    expectThat(recordedEvents.filterIsInstance<HttpEvent.Outgoing>()).hasSize(2)
  }

  @Test
  fun `reports uncaught exceptions`() {
    val recordedEvents = mutableListOf<Event>()
    val app = TestApp(
      raServer = { throw RuntimeException("boom") },
      events = recordedEvents::add
    )

    app(
      Request(POST, "/artists").header(
        Header.CONTENT_TYPE.meta.name,
        ContentType.APPLICATION_FORM_URLENCODED.toHeaderValue()
      ).form("url", "https://ra.com/dj/someone")
    )

    expectThat(recordedEvents.filterIsInstance<UncaughtExceptionEvent>()).hasSize(1)
  }

  @Test
  fun `Telegram security is active`() {
    val app = TestApp(users = listOf(UserId(1)))

    expectThat(app.postTelegramMessage(from = Message.From(id = 2)).status)
      .isEqualTo(UNAUTHORIZED)
  }
}
