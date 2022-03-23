package zhelenskiy.ru.models.admin

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDateTime
import zhelenskiy.ru.commands.Command
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.dataclasses.Subscription
import zhelenskiy.ru.models.common.CommonModel
import kotlin.random.Random
import kotlin.random.nextULong

fun CommandStorage<CommonModel>.ReadOnlyView.subscriptionInfo(subscriptionId: ULong): Subscription? =
    current.subscriptions[subscriptionId]

fun CommandStorage<CommonModel>.ReadOnlyView.updateSubscription(subscriptionId: ULong, newDate: LocalDateTime) =
    update(UpdateSubscription(subscriptionId, newDate))

data class UpdateSubscription(val subscriptionId: ULong, val newDate: LocalDateTime) :
    Command.Transformer<CommonModel> {
    override fun transform(before: CommonModel): CommonModel? = before.mutate { model ->
        model.subscriptions[subscriptionId] = model.subscriptions[subscriptionId]
            ?.takeIf { it.validUntil < newDate }
            ?.mutate { it.validUntil = newDate }
            ?: return null
    }
}


fun CommandStorage<CommonModel>.ReadOnlyView.newSubscription(clientId: ULong, date: LocalDateTime) =
    update(NewSubscription(clientId, date))


data class NewSubscription(val clientId: ULong, val date: LocalDateTime) : Command.Transformer<CommonModel> {
    override fun transform(before: CommonModel): CommonModel? = before.mutate { model ->
        model.people[clientId] = model.people[clientId]
            ?.mutate { person ->
                var subscriptionId: ULong
                val subscription = Subscription(date, persistentListOf())
                do {
                    subscriptionId = Random.nextULong()
                } while (model.subscriptions.putIfAbsent(subscriptionId, subscription) != null)
                person.subscriptions.add(subscriptionId)
            }
            ?: return null
    }
}