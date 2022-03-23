package zhelenskiy.ru

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class AdminTest : AbstractReportsTest() {
    private fun serialized(instant: Instant) = Json.encodeToString(instant.toLocalDateTime(TimeZone.UTC))

    @Test
    fun subscriptionInfo() = testConfiguredApplication {
        try {
            client.get("/admin/subscriptionInfo/100500")
            throw AssertionError("Should fail with 404 status code")
        } catch (exception: ClientRequestException) {
            val expected =
                "Client request(GET http://localhost/admin/subscriptionInfo/100500) invalid: 404 Not Found. Text: \"\""
            assertEquals(expected, exception.message)
        }
        client.get("/admin/subscriptionInfo/0").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                expected = """{"validUntil":"-100000-01-10T23:59:59.999999999",
                                    |"entries":[
                                    |{"first":"-100001-12-31T23:59:59.999999999","second":"IN"},
                                    |{"first":"-100000-01-01T00:09:59.999999999","second":"OUT"},
                                    |{"first":"-100000-01-01T00:19:59.999999999","second":"IN"},
                                    |{"first":"-100000-01-01T00:39:59.999999999","second":"OUT"},
                                    |{"first":"-100000-01-01T00:49:59.999999999","second":"IN"},
                                    |{"first":"-100000-01-01T01:19:59.999999999","second":"OUT"}
                                    |]}""".trimMargin().replace("\n", ""),
                actual = bodyAsText()
            )
        }
    }

    @Test
    fun prolongSubscription() = testConfiguredApplication {
        client.put("/admin/prolongSubscription/100500") {
            this.contentType(ContentType.Application.Json)
            this.setBody(serialized(Instant.DISTANT_FUTURE + 10.days))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertFalse(bodyAsText().toBooleanStrict())
        }
        client.put("/admin/prolongSubscription/2") {
            this.contentType(ContentType.Application.Json)
            this.setBody(serialized(Instant.DISTANT_FUTURE - 10.days))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertFalse(bodyAsText().toBooleanStrict())
        }
        client.put("/admin/prolongSubscription/2") {
            contentType(ContentType.Application.Json)
            setBody(serialized(Instant.DISTANT_FUTURE + 10.days))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().toBooleanStrict())
        }
    }

    @Test
    fun newSubscription() = testConfiguredApplication {
        client.post("/admin/newSubscription/100500") {
            this.contentType(ContentType.Application.Json)
            this.setBody(serialized(Instant.DISTANT_FUTURE + 10.days))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertFalse(bodyAsText().toBooleanStrict())
        }
        client.post("/admin/newSubscription/0") {
            this.contentType(ContentType.Application.Json)
            this.setBody(serialized(Instant.DISTANT_FUTURE - 10.days))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().toBooleanStrict())
        }
        client.post("/admin/newSubscription/0") {
            contentType(ContentType.Application.Json)
            setBody(serialized(Instant.DISTANT_FUTURE + 10.days))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().toBooleanStrict())
        }
    }
}