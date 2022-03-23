package zhelenskiy.ru.commands

// event for a single property
sealed interface Command<out T: Any> {
    fun interface Producer<out T: Any> : Command<T> {
        fun makeValue(): T
    }

    fun interface Transformer<T: Any> : Command<T> {
        fun transform(before: T): T?
    }
}

data class SimpleProducer<T: Any>(private val value: T) : Command.Producer<T> {
    override fun makeValue(): T = value
}

infix fun <T: Any> Command.Producer<T>.compose(transformer: Command.Transformer<T>) =
    Command.Producer { this@compose.makeValue().let{ transformer.transform(it) ?: it } }

infix fun <T: Any> Command.Transformer<T>.compose(transformer: Command.Transformer<T>) =
    Command.Transformer<T> { transformer.transform(this.transform(it) ?: it) }

infix fun <T: Any> Command<T>.compose(transformer: Command.Transformer<T>) = when (this) {
    is Command.Producer -> compose(transformer)
    is Command.Transformer -> compose(transformer)
}

interface WithIdentity<T> {
    val id: ULong
}

inline fun <K, V> MutableMap<K, V>.mutateValue(key: K, onEmpty: () -> Unit = {}, mutator: (V) -> V) {
    val oldValue = this[key] ?: return onEmpty()
    this[key] = mutator(oldValue)
}