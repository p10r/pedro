package de.p10r

import de.p10r.infrastructure.AppOutgoingHttp
import de.p10r.infrastructure.Features
import de.p10r.ra.RAClient
import de.p10r.telegram.TelegramClient
import de.p10r.telegram.TelegramConfig
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
