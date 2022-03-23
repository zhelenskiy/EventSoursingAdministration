package zhelenskiy.ru.commands

import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.random.Random
import kotlin.random.nextULong
import kotlin.reflect.KProperty

class CommandStorage<T: Any> {
    data class EventResult<T: Any>(val command: Command<T>, val result: T)

    private val history: MutableMap<ULong, MutableList<EventResult<T>>> = ConcurrentHashMap()

    private tailrec fun makeId(listToPut: MutableList<EventResult<T>>): ULong =
        Random.nextULong().takeIf { history.putIfAbsent(it, listToPut) == null } ?: makeId(listToPut)

    private fun store(producer: Command.Producer<T>): ULong {
        val computed = producer.makeValue()
        val listToPut = mutableListOf(EventResult(producer, computed))
        return makeId(listToPut)
    }

    private tailrec fun update(id: ULong, transformer: Command.Transformer<T>): Boolean {
        val localHistory = showHistoryImpl(id)
        val oldLastIndex = localHistory.lastIndex
        val oldValue = localHistory[oldLastIndex].result
        val newValue = transformer.transform(oldValue) ?: return false
        synchronized(localHistory) { // synchronizing only to add
            if (localHistory.lastIndex == oldLastIndex) {
                localHistory.add(EventResult(transformer, newValue))
                return true
            }
        }
        return update(id, transformer)
    }

    private fun showHistoryImpl(id: ULong): MutableList<EventResult<T>> = history[id] ?: throw notFoundException(id)

    private fun showHistory(id: ULong): List<EventResult<T>> = showHistoryImpl(id)

    private fun notFoundException(id: ULong) = NoSuchElementException("Id $id does not refer to any value")

    private fun getLastValue(id: ULong): T = showHistory(id).last().result

    open inner class ReadOnlyView(override val id: ULong) : ReadOnlyProperty<Any?, T>, WithIdentity<T> {

        constructor(init: Command.Producer<T>) : this(store(init))
        constructor(initValue: T) : this(SimpleProducer(initValue))

        val current: T
            get() = getLastValue(id)

        fun update(transformer: Command.Transformer<T>) = update(id, transformer)

        fun showHistory() = showHistory(id)

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = current
    }
}