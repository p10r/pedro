package de.p10r

import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.db.DynamoDbConfig
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.telegram.TelegramClient
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driving.ApiRoutes
import de.p10r.domain.ArtistsRegistry
import de.p10r.infrastructure.AppOutgoingHttp
import de.p10r.infrastructure.Features
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.events.Events

data class UserId(val value: Int)

fun App(
  dynamoDbConfig: DynamoDbConfig,
  raUri: Uri,
  raHttp: HttpHandler,
  events: Events,
  telegramConfig: TelegramConfig,
  users: List<UserId>,
  @Suppress("UNUSED_PARAMETER") features: Features,
): HttpHandler {
  val artistRepository = ArtistRepository(
    dynamoDbConfig.copy(http = AppOutgoingHttp(events, dynamoDbConfig.http)),
  )
  val raClient = RAClient(raUri, AppOutgoingHttp(events, raHttp))
  val artistsRegistry = ArtistsRegistry(artistRepository, raClient)
  //TODO remove events as last parameter
  val telegramClient = TelegramClient(telegramConfig, events)


  return ApiRoutes(
    followArtist = artistsRegistry::add,
    listAllArtists = artistsRegistry::list,
    secret = telegramConfig.secret,
    users = users,
    telegramClient::sendMessage,
    events
  )
}
