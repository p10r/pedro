package de.p10r

import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.db.DynamoDbConfig
import de.p10r.adapters.driven.ra.RAClient
import de.p10r.adapters.driven.telegram.TelegramConfig
import de.p10r.adapters.driving.ApiRoutes
import de.p10r.domain.UserCommandHub
import de.p10r.domain.UserId
import de.p10r.infrastructure.AppOutgoingHttp
import de.p10r.infrastructure.Features
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.events.Events

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
  val userCommandHub = UserCommandHub(artistRepository, raClient)

  return ApiRoutes(
    processTelegramCommands = userCommandHub::process,
    listAllArtists = artistRepository::findAll,
    secret = telegramConfig.secret,
    users = users,
    events
  )
}
