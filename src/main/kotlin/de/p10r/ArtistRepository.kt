package de.p10r

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.resultFrom
import dev.forkhandles.values.ofOrNull
import org.http4k.aws.AwsCredentials
import org.http4k.connect.amazon.core.model.Region
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.filter.debug


interface ArtistRepository {
  fun findAll(): Result4k<List<Artist>, Exception>
  fun findByName(name: String): Result<Artist?, Exception>
  fun create(newArtist: NewArtist): Result<Artist, Exception>
}

class SqliteArtistRepository(database: Database) : ArtistRepository {
  private val sql = database.artistQueries

  override fun findAll(): Result4k<List<Artist>, Exception> =
    resultFrom { sql.selectAll().executeAsList().toArtists() }

  override fun findByName(name: String): Result<Artist?, Exception> =
    resultFrom { sql.findByName(name).executeAsOneOrNull()?.toArtist() }

  override fun create(newArtist: NewArtist): Result<Artist, Exception> = resultFrom {
    val id = sql.create(newArtist.name).executeAsOne()
    Artist(ArtistId.of(id), newArtist.name)
  }

  private fun Stored_artists.toArtist() = Artist(ArtistId.ofOrNull(id)!!, name)

  private fun List<Stored_artists>.toArtists() = map { it.toArtist() }
}

class DynamoDbRepository : ArtistRepository {
  val http = FakeDynamoDb()
  val dynamoDb = DynamoDb.Http(
    region = Region.CA_CENTRAL_1,
    credentialsProvider = { AwsCredentials("id", "secret") },
    http = http.debug()
  )
  private val table: DynamoDbTableMapper<ArtistEntity, String, Unit> =
    dynamoDb.tableMapper<ArtistEntity, String, Unit>(
      tableName = TableName.of("artists"),
      hashKeyAttribute = Attribute.string().required("id")
    ).also {
      it.createTable()
    }

  override fun findAll(): Result4k<List<Artist>, Exception> = resultFrom {
    table.primaryIndex().scan().toList().map { it.toArtist() }
  }

  override fun findByName(name: String): Result<Artist?, Exception> =
    findAll().map { artists ->
      artists.firstOrNull { artist ->
        artist.name.lowercase() == name.lowercase()
      }
    }

  override fun create(newArtist: NewArtist): Result<Artist, Exception> = resultFrom {
    val artist = newArtist.toArtist()
    table.save(ArtistEntity.of(artist))
    artist
  }

  var idGenerator: Long = 1
  private fun NewArtist.toArtist() = Artist(ArtistId.of(idGenerator++), name)

  private data class ArtistEntity(
    val id: String,
    val name: String
  ) {
    companion object {
      fun of(artist: Artist) = ArtistEntity(artist.id.toString(), artist.name)
    }

    fun toArtist() = Artist(ArtistId.of(id.toLong()), name)
  }
}
