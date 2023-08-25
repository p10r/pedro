package de.p10r

import de.p10r.fixtures.TestApp
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

@Disabled
class E2ETest {
  @Test
  fun `adds an artist send via Telegram`() {
    val validUserId = 444

    val secret = TelegramSecret("s3cr3t")
    val app = TestApp(
      existingArtists = emptyList(),
      users = listOf(UserId(validUserId)),
      secret = secret
    )

    val req = Request(Method.POST, "/telegram")
      .header(TELEGRAM_SECRET_HEADER, secret.value)
      .body(
        """
      {
        "update_id": 970580115,
        "message": {
          "message_id": 13,
          "from": {
            "id": 444,
            "is_bot": false,
            "first_name": "Philipp",
            "username": "yourname",
            "language_code": "en"
          },
          "chat": {
            "id": $validUserId,
            "first_name": "Philipp",
            "username": "yourname",
            "type": "private"
          },
          "date": 1691939664,
          "text": "/add https://ra.co/dj/sabura",
          "entities": [
            {
              "offset": 0,
              "length": 4,
              "type": "bot_command"
            },
            {
              "offset": 5,
              "length": 26,
              "type": "url"
            }
          ]
        }
      }
    """.trimIndent()
      )

    val receivedTelegramMessages = mutableListOf<String>()
    val telegramServer = { req: Request ->
      receivedTelegramMessages.add(req.bodyString())
      Response(OK)
    }

    expectThat(app.getAllArtists()).isEmpty()

    val postMessageRes = app(req)

    expectThat(postMessageRes.status).isEqualTo(OK)
    expectThat(app.getAllArtists()).isNotEmpty()
    expectThat(receivedTelegramMessages).isNotEmpty()
  }

  private fun HttpHandler.getAllArtists() = this(Request(GET, "/artists")).let(artists)
}
