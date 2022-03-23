package zhelenskiy.ru.models.turnstile

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import zhelenskiy.ru.dataclasses.EntryType
import zhelenskiy.ru.commands.Command
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.models.common.CommonModel

fun CommandStorage<CommonModel>.ReadOnlyView.tryToEnter(
    subscriptionId: ULong, entryType: EntryType, clock: Clock
): Boolean {
    val entryTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return update(EntranceAttempt(subscriptionId, entryType, entryTime))
}

@kotlinx.serialization.Serializable
data class EntranceAttempt(val subscriptionId: ULong, val entryType: EntryType, val entryTime: LocalDateTime) :
    Command.Transformer<CommonModel> {
    override fun transform(before: CommonModel): CommonModel? = before.mutate { model ->
        model.subscriptions[subscriptionId] = model.subscriptions[subscriptionId]
            ?.takeIf { it.validUntil >= entryTime }
            ?.mutate { it.entries.add(entryTime to entryType) }
            ?: return null
    }
}
