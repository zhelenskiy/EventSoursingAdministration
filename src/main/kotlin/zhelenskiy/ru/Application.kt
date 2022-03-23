package zhelenskiy.ru

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import kotlinx.datetime.*
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.configuration.SimpleConfiguration
import zhelenskiy.ru.models.common.CommonModel
import zhelenskiy.ru.models.reports.ReportingSystem
import zhelenskiy.ru.plugins.*

fun main() {
    val configuration = SimpleConfiguration()
    with(configuration) {
        embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            module(configuration)
        }.start(wait = true)
    }
}

private fun Application.module(
    simpleConfiguration: SimpleConfiguration
) {
    configureSerialization()
    configureRouting(
        simpleConfiguration.mainStorage,
        simpleConfiguration.statisticsStorage,
        simpleConfiguration.clock
    )
}
