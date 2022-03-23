package zhelenskiy.ru.models.common

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import zhelenskiy.ru.dataclasses.Person
import zhelenskiy.ru.dataclasses.Subscription
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
data class CommonModel(
    val people: PersistentMap<ULong, Person> = persistentMapOf(),
    val subscriptions: PersistentMap<ULong, Subscription> = persistentMapOf(),
) {
    inner class Builder {
        val people: PersistentMap.Builder<ULong, Person> = this@CommonModel.people.builder()
        val subscriptions: PersistentMap.Builder<ULong, Subscription> = this@CommonModel.subscriptions.builder()
    }

    inline fun mutate(mutator: (Builder) -> Unit): CommonModel {
        contract {
            callsInPlace(mutator, InvocationKind.EXACTLY_ONCE)
        }
        return Builder().apply(mutator).let { CommonModel(it.people.build(), it.subscriptions.build()) }
    }
}

