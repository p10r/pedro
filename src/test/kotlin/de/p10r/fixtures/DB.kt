package de.p10r.fixtures

import de.p10r.adapters.driven.db.ArtistRepository
import de.p10r.adapters.driven.db.DynamoDbConfig
import de.p10r.domain.NewArtist
import org.http4k.aws.AwsCredentials
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.createTable
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.AttributeName
import org.http4k.connect.amazon.dynamodb.model.KeySchema
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.connect.amazon.dynamodb.model.asAttributeDefinition
import org.http4k.connect.amazon.dynamodb.model.compound
import org.http4k.core.Uri

fun dynamoDbConfig() = DynamoDbConfig(
  uri = Uri.of("http://dynamo-db"),
  http = FakeDynamoDb().also { it.client().createPedroTables() },
  credentials = AwsCredentials("id", "secret")
)

fun DynamoDb.createPedroTables() =
  createTable(
    TableName = TableName.of("artists"),
    KeySchema = KeySchema.compound(AttributeName.of("id")),
    AttributeDefinitions = listOf(Attribute.string().required("id").asAttributeDefinition())
  )

fun ArtistRepository.Companion.new(
  existingArtists: List<NewArtist> = emptyList()
): ArtistRepository = ArtistRepository(dynamoDbConfig()).apply {
  existingArtists.forEach { artist -> this.save(artist) }
}
