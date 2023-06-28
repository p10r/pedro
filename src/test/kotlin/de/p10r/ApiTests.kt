package de.p10r

import org.http4k.core.ContentType
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.body.form
import org.http4k.events.Event
import org.http4k.events.HttpEvent
import org.http4k.lens.Header
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize

//TODO check same as UiTests, use contract
class ApiTests {
  val recordedEvents = mutableListOf<Event>()
  val app = TestApp(events = recordedEvents::add)

  @Test
  fun `reports every incoming http tx`() {
    app(Request(GET, "/artists"))
    app(Request(GET, "/"))
    app(Request(POST, "/artists"))

    expectThat(recordedEvents.filterIsInstance<HttpEvent.Incoming>()).hasSize(3)
  }

  @Test
  fun `reports outgoing http tx`() {
    app(
      Request(POST, "/artists").header(
        Header.CONTENT_TYPE.meta.name,
        ContentType.APPLICATION_FORM_URLENCODED.toHeaderValue()
      ).form("url", "http://ra.com/dj/someone")
    )

    expectThat(recordedEvents.filterIsInstance<HttpEvent.Incoming>()).hasSize(1)
    expectThat(recordedEvents.filterIsInstance<HttpEvent.Outgoing>()).hasSize(1)
  }

  @Test
  fun `reports uncaught exceptions`() {
    TestApp(
      raServer = { throw RuntimeException("boom") },
      events = recordedEvents::add
    )(
      Request(POST, "/artists").header(
        Header.CONTENT_TYPE.meta.name,
        ContentType.APPLICATION_FORM_URLENCODED.toHeaderValue()
      ).form("url", "http://ra.com/dj/someone")
    )

    expectThat(recordedEvents.filterIsInstance<UncaughtExceptionEvent>()).hasSize(1)
  }
}
