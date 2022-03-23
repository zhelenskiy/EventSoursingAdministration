package zhelenskiy.ru

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import zhelenskiy.ru.dataclasses.EntryType
import zhelenskiy.ru.models.turnstile.tryToEnter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class EntranceTest : AbstractReportsTest() {
    @Test
    fun unitTest() = testConfiguredApplication { configuration ->
        val oldSize = configuration.mainStorage.showHistory().size
        for (subscriptionId in 0UL..1UL) {
            assertFalse(configuration.mainStorage.tryToEnter(subscriptionId, EntryType.IN, Clock.System))
            assertEquals(oldSize, configuration.mainStorage.showHistory().size)
        }
        assertTrue(configuration.mainStorage.tryToEnter(2UL, EntryType.IN, Clock.System))
        assertEquals(oldSize + 1, configuration.mainStorage.showHistory().size)
    }

    @Test
    fun integrationTest() = testConfiguredApplication {
        for (command in listOf("in", "out")) {
            client.post("/turnstile/tryAccess/0/$command").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertFalse { bodyAsText().toBooleanStrict() }
            }
            client.post("/turnstile/tryAccess/2/$command").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertTrue { bodyAsText().toBooleanStrict() }
            }
        }
    }
}