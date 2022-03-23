package zhelenskiy.ru.configuration

import kotlinx.datetime.Clock
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.models.common.CommonModel
import zhelenskiy.ru.models.reports.ReportingSystem

interface Configuration {
    val mainStorage: CommandStorage<CommonModel>.ReadOnlyView
    val statisticsStorage: ReportingSystem
    val clock: Clock
}