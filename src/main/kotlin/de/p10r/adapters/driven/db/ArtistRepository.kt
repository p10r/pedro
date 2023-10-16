package de.p10r.adapters.driven.db

import de.p10r.domain.models.Artist
import de.p10r.domain.models.ArtistId
import de.p10r.domain.models.NewArtist
import de.p10r.domain.models.UserId
import org.http4k.aws.AwsCredentials
import org.http4k.connect.amazon.core.model.Region
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters


data class DynamoDbConfig(
  val uri: Uri,
  val http: HttpHandler,
  val credentials: AwsCredentials,
)

// TODO: Log error events
class ArtistRepository(config: DynamoDbConfig) {
  companion object {}

  private val dynamoDb = DynamoDb.Http(
    region = Region.EU_CENTRAL_1,
    credentialsProvider = { config.credentials },
    http = ClientFilters.SetBaseUriFrom(config.uri).then(config.http)
  )

  private val table: DynamoDbTableMapper<ArtistEntity, String, Unit> =
    dynamoDb.tableMapper<ArtistEntity, String, Unit>(
      tableName = TableName.of("artists"),
      hashKeyAttribute = Attribute.string().required("id")
    )

  fun findAllBy(userId: UserId): List<Artist> =
    table.primaryIndex()
      .scan()
      .toList()
      .filter { it.followedBy.contains(userId) }
      .map { entity -> Artist(ArtistId.of(entity.id), entity.name) }

  fun findAll(): List<Artist> =
    findAllEntities()
      .map { entity -> Artist(ArtistId.of(entity.id), entity.name) }

  fun findByName(name: String): Artist? =
    findAll().firstOrNull { artist ->
      artist.name.lowercase() == name.lowercase()
    }

  fun save(newArtist: NewArtist, userId: UserId? = null): Artist {
    val entity: ArtistEntity? = findEntityByName(newArtist.name)
    println("saving $newArtist for userId $userId")
    if (entity == null) {
      val artist = Artist(ArtistId.new(), newArtist.name)
      table.save(ArtistEntity.of(artist, userId))
      return artist
    }

    if (userId != null) {
      val document = entity.copy(followedBy = entity.followedBy + userId)
      println("saving updated $document")
      table.save(document)
      return Artist(ArtistId(entity.id), entity.name)
    }

    return Artist(ArtistId(entity.id), entity.name)
  }

  private fun findAllEntities(): List<ArtistEntity> =
    table.primaryIndex()
      .scan()
      .toList()

  private fun findEntityByName(name: String): ArtistEntity? =
    findAllEntities().firstOrNull { artist ->
      artist.name.lowercase() == name.lowercase()
    }

  private data class ArtistEntity(
    val id: String,
    val name: String,
    val followedBy: Set<UserId>
  ) {
    companion object {
      fun of(artist: Artist, userId: UserId?) = ArtistEntity(
        id = artist.id.toString(),
        name = artist.name,
        followedBy = if (userId == null) emptySet() else setOf(userId)
      )
    }
  }
}
