package de.p10r.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.then
import org.http4k.events.Event
import org.http4k.events.Event.Companion.Error
import org.http4k.events.Events
import org.http4k.events.HttpEvent
import org.http4k.filter.ClientFilters
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson
import org.http4k.routing.RoutingHttpHandler
import java.time.Instant

fun AppIncomingHttp(events: Events, base: RoutingHttpHandler) =
  HandleError(events)
    .then(ServerFilters.RequestTracing())
    .then(ResponseFilters.ReportHttpTransaction { events(HttpEvent.Incoming(it)) })
    .then(base)

fun AppOutgoingHttp(events: Events, http: HttpHandler) =
  ClientFilters.RequestTracing()
    .then(ResponseFilters.ReportHttpTransaction { events(HttpEvent.Outgoing(it)) })
    .then(http)

fun HandleError(events: Events) = ServerFilters.CatchAll {
  events(Error("uncaught!", it))
  Response(INTERNAL_SERVER_ERROR)
}

data class UncaughtExceptionEvent(
  val message: String,
  val stackTrace: List<String>
) : Event {
  constructor(exception: Throwable) : this(
    exception.message.orEmpty(),
    exception.stackTrace.map(StackTraceElement::toString)
  )
}


fun loggingEvents(
  logger: (String) -> Unit = ::println,
  objectMapper: ObjectMapper = Jackson.mapper,
  clock: () -> Instant = Instant::now
): Events = { event: Event ->
  logger(
    objectMapper.writeValueAsString(
      LogMessage(
        timestamp = clock(),
        event = event::class.simpleName ?: "Unknown Event",
        data = event
      )
    )
  )
}

@Suppress("unused")
class LogMessage(val timestamp: Instant, val event: String, val data: Event)

infix fun Events.then(that: Events): Events = { event ->
  this(event)
  that(event)
}
