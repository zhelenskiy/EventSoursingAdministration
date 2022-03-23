package zhelenskiy.ru.models.reports

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import zhelenskiy.ru.commands.Command
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.dataclasses.EntryType.IN
import zhelenskiy.ru.dataclasses.EntryType.OUT
import zhelenskiy.ru.models.common.CommonModel
import zhelenskiy.ru.models.turnstile.EntranceAttempt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ReportingSystem(private val model: CommandStorage<CommonModel>.ReadOnlyView) {
    private var currentMoment = 0
    private val history: List<CommandStorage.EventResult<CommonModel>>
        get() = model.showHistory()

    private val dailyStatistics: MutableMap<LocalDate, Int> = mutableMapOf()

    @Synchronized
    private fun updateStatistics() {
        while (currentMoment < history.size) {
            when (val command = history[currentMoment].command) {
                is Command.Producer -> Unit
                is Command.Transformer -> handleTransformer(command, currentMoment)
            }
            currentMoment++
        }
    }

    private var totalAttendedCount = 0
    private var totalAttendedDuration = 0.milliseconds

    private fun handleTransformer(transformer: Command.Transformer<CommonModel>, storyMoment: Int) {
        if (transformer !is EntranceAttempt) return
        val entries = history[storyMoment].result.subscriptions[transformer.subscriptionId]!!.entries
        if (entries.size < 2) return
        val (preLastTime, preLastType) = entries[entries.lastIndex - 1]
        val (lastTime, lastType) = entries.last()
        if (preLastType != IN || lastType != OUT || preLastTime > lastTime) return // some strange situation
        val preLastTimeToDate = preLastTime.date
        val currentValue = dailyStatistics[preLastTimeToDate] ?: 0
        dailyStatistics[preLastTimeToDate] = currentValue + 1
        totalAttendedCount++
        totalAttendedDuration += lastTime.toInstant(TimeZone.UTC) - preLastTime.toInstant(TimeZone.UTC)
    }

    @Synchronized
    fun getDailyStatistics(): Map<LocalDate, Int> {
        updateStatistics()
        return dailyStatistics
    }

    @Synchronized
    fun getAverageDuration(): Duration? {
        updateStatistics()
        return if (totalAttendedCount > 0) totalAttendedDuration / totalAttendedCount else null
    }
}