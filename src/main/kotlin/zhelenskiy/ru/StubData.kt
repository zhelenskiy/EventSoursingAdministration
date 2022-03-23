package zhelenskiy.ru

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.dataclasses.EntryType
import zhelenskiy.ru.dataclasses.Person
import zhelenskiy.ru.dataclasses.Subscription
import zhelenskiy.ru.models.common.CommonModel
import zhelenskiy.ru.models.turnstile.EntranceAttempt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun CommandStorage<CommonModel>.ReadOnlyView.addStubData() {
    val past = Instant.DISTANT_PAST

    fun pastPlus(duration: Duration) = (past + duration).toLocalDateTime(TimeZone.UTC)

    val entries = persistentListOf(
        pastPlus(0.minutes) to EntryType.IN,
        pastPlus(10.minutes) to EntryType.OUT,
        pastPlus(20.minutes) to EntryType.IN,
        pastPlus(40.minutes) to EntryType.OUT,
        pastPlus(50.minutes) to EntryType.IN,
        pastPlus(80.minutes) to EntryType.OUT,
    )

    update {
        it.mutate { model ->
            model.people[0UL] = Person("Person One", persistentSetOf(0UL, 1UL))
            model.people[1UL] = Person("Person Two", persistentSetOf(2UL, 3UL))
            model.subscriptions[0UL] = Subscription(pastPlus(10.days), persistentListOf())
            model.subscriptions[2UL] =
                Subscription(Instant.DISTANT_FUTURE.toLocalDateTime(TimeZone.UTC), persistentListOf())
        }
    }

    for ((entryTime, entryType) in entries) {
        update(EntranceAttempt(0UL, entryType, entryTime))
        update(EntranceAttempt(2UL, entryType, entryTime))
    }
}