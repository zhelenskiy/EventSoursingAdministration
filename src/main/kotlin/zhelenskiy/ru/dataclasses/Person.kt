package zhelenskiy.ru.dataclasses

import kotlinx.collections.immutable.PersistentSet
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@Serializable
data class Person(val name: String, val subscriptions: PersistentSet<ULong>) {
    inner class Builder {
        var name: String = this@Person.name
        val subscriptions: PersistentSet.Builder<ULong> = this@Person.subscriptions.builder()
    }

    inline fun mutate(mutator: (Builder) -> Unit): Person {
        contract {
            callsInPlace(mutator, InvocationKind.EXACTLY_ONCE)
        }
        return Builder().apply(mutator).let { Person(it.name, it.subscriptions.build())}
    }
}