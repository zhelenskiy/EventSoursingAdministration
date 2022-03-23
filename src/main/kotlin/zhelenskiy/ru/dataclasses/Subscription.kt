package zhelenskiy.ru.dataclasses

import kotlinx.collections.immutable.PersistentList
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
data class Subscription(
    val validUntil: LocalDateTime,
    val entries: PersistentList<Pair<LocalDateTime, EntryType>>,
) {
    inner class Builder {
        var validUntil: LocalDateTime = this@Subscription.validUntil
        val entries: PersistentList.Builder<Pair<LocalDateTime, EntryType>> = this@Subscription.entries.builder()
    }

    inline fun mutate(mutator: (Builder) -> Unit): Subscription {
        contract {
            callsInPlace(mutator, InvocationKind.EXACTLY_ONCE)
        }
        return Builder().apply(mutator).let { Subscription(it.validUntil, it.entries.build()) }
    }

    fun toSerializable(): SerializableSubscription = SerializableSubscription(validUntil, entries.toList())

    @Serializable
    data class SerializableSubscription(val validUntil: LocalDateTime, val entries: List<Pair<LocalDateTime, EntryType>>)
}

enum class EntryType { IN, OUT }
