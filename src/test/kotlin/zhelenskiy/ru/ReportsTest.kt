package zhelenskiy.ru

import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import kotlin.time.Duration.Companion.minutes

class ReportsTest : AbstractReportsTest() {

    @Test
    fun testDailyDiagnostics() = testConfiguredApplication {
        client.get("/reports/dailyStatistics").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("""{"-100001-12-31":2,"-100000-01-01":4}""", bodyAsText())
        }
    }

    @Test
    fun testAverageDiagnostics() = testConfiguredApplication {
        client.get("/reports/averageDuration").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(20.minutes.toIsoString(), bodyAsText())
        }
    }
}