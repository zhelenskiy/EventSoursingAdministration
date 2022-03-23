package zhelenskiy.ru.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.dataclasses.EntryType
import zhelenskiy.ru.models.admin.newSubscription
import zhelenskiy.ru.models.admin.subscriptionInfo
import zhelenskiy.ru.models.admin.updateSubscription
import zhelenskiy.ru.models.common.CommonModel
import zhelenskiy.ru.models.reports.ReportingSystem
import zhelenskiy.ru.models.turnstile.tryToEnter
import kotlin.time.Duration

fun Application.configureRouting(
    mainStorage: CommandStorage<CommonModel>.ReadOnlyView,
    statisticsStorage: ReportingSystem,
    clock: Clock,
) {
    routing {
        managementControl(mainStorage)
        turnstileControl(mainStorage, clock)
        reportControl(statisticsStorage)
    }
}

private fun Routing.reportControl(statisticsStorage: ReportingSystem) {
    route("/reports") {
        get("/dailyStatistics") {
            val message = statisticsStorage.getDailyStatistics()
            call.respond(Json.encodeToString(message))
        }
        get("/averageDuration") {
            statisticsStorage.getAverageDuration()?.let { call.respond(it.toIsoString()) }
                ?: call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun Routing.turnstileControl(mainStorage: CommandStorage<CommonModel>.ReadOnlyView, clock: Clock) {
    route("/turnstile") {
        post("/tryAccess/{subscription_id}/{action_type}") {
            val subscriptionId = call.parameters["subscription_id"]?.toULongOrNull() ?: return@post noSubscriptionId()
            val actionType = when (val type = call.parameters["action_type"]?.toLowerCasePreservingASCIIRules()) {
                "out" -> EntryType.OUT
                "in" -> EntryType.IN
                null -> return@post call.respond(HttpStatusCode.BadRequest, "No action type is specified")
                else -> return@post call.respond(HttpStatusCode.BadRequest, "Unknown action type: $type")
            }
            call.respond(
                HttpStatusCode.OK,
                Json.encodeToString(mainStorage.tryToEnter(subscriptionId, actionType, clock))
            )
        }
    }
}

private fun Routing.managementControl(mainStorage: CommandStorage<CommonModel>.ReadOnlyView) {
    route("/admin") {
        get("/subscriptionInfo/{subscription_id}") {
            val subscriptionId = call.parameters["subscription_id"]?.toULongOrNull() ?: return@get noSubscriptionId()
            call.respond(
                Json.encodeToString(
                    mainStorage.subscriptionInfo(subscriptionId)?.toSerializable()
                        ?: return@get call.respond(HttpStatusCode.NotFound)
                )
            )
        }
        put("/prolongSubscription/{subscription_id}") {
            val subscriptionId = call.parameters["subscription_id"]?.toULongOrNull() ?: return@put noSubscriptionId()
            val newDate = Json.decodeFromString<LocalDateTime>(call.receive())
            call.respond(mainStorage.updateSubscription(subscriptionId, newDate).toString())
        }
        post("/newSubscription/{client_id}") {
            val clientId = call.parameters["client_id"]?.toULongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "No client id found")
            val date = Json.decodeFromString<LocalDateTime>(call.receive())
            call.respond(mainStorage.newSubscription(clientId, date).toString())
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.noSubscriptionId() {
    call.respond(HttpStatusCode.BadRequest, "No subscription id found")
}
