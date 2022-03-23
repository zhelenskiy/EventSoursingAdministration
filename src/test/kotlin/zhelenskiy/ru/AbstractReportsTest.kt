package zhelenskiy.ru

import io.ktor.server.application.*
import io.ktor.server.testing.*
import zhelenskiy.ru.configuration.Configuration
import zhelenskiy.ru.configuration.SimpleConfiguration
import zhelenskiy.ru.plugins.configureRouting

open class AbstractReportsTest {
    protected fun testConfiguredApplication(
        block: suspend ApplicationTestBuilder.(configuration: Configuration) -> Unit
    ): Unit = testApplication {
        val configuration = SimpleConfiguration()
        application {
            with(configuration) {
                configureRouting(mainStorage, statisticsStorage, clock)
            }
        }
        block(configuration)
    }
}