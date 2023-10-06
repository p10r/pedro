package de.p10r

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

class ArtistRepository(config: DynamoDbConfig) {
  companion object {}

  private val dynamoDb = DynamoDb.Http(
    region = Region.CA_CENTRAL_1,
    credentialsProvider = { config.credentials },
    http = ClientFilters.SetBaseUriFrom(config.uri).then(config.http)
  )

  private val table: DynamoDbTableMapper<ArtistEntity, String, Unit> =
    dynamoDb.tableMapper<ArtistEntity, String, Unit>(
      tableName = TableName.of("artists"),
      hashKeyAttribute = Attribute.string().required("id")
    ).also {
      it.createTable()
    }

  fun findAll(): List<Artist> =
    table.primaryIndex()
      .scan()
      .toList()
      .map { entity -> Artist(ArtistId.of(entity.id), entity.name) }

  fun findByName(name: String): Artist? =
    findAll().firstOrNull { artist ->
      artist.name.lowercase() == name.lowercase()
    }


  fun create(newArtist: NewArtist): Artist {
    val artist = Artist(ArtistId.new(), newArtist.name)
    table.save(ArtistEntity.of(artist))
    return artist
  }

  private data class ArtistEntity(
    val id: String,
    val name: String
  ) {
    companion object {
      fun of(artist: Artist) = ArtistEntity(artist.id.toString(), artist.name)
    }
  }
}
