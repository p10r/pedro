package de.p10r

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApprovalTest::class)
class ListArtistTests {
  val app = TestApp()

  @Test
  fun `lists already stored artists`(approver: Approver) {
    val response = app(Request(Method.GET, "/"))

    assertEquals(Response(Status.OK), response)
  }
}
