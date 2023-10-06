package de.p10r.fixtures

import de.p10r.ArtistRepository
import de.p10r.DynamoDbConfig
import de.p10r.NewArtist
import org.http4k.aws.AwsCredentials
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.core.Uri

val dynamoDbConfig = DynamoDbConfig(
  uri = Uri.of("http://dynamo-db"),
  http = FakeDynamoDb(),
  credentials = AwsCredentials("id", "secret")
)

fun ArtistRepository.Companion.new(
  existingArtists: List<NewArtist> = emptyList()
): ArtistRepository = ArtistRepository(
  DynamoDbConfig(
    uri = Uri.of("http://dynamo-db"),
    http = FakeDynamoDb(),
    credentials = AwsCredentials("id", "secret")
  )
).apply {
  existingArtists.forEach { artist -> this.create(artist) }
}
