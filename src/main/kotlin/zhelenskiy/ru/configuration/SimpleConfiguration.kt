package zhelenskiy.ru.configuration

import kotlinx.datetime.Clock
import zhelenskiy.ru.addStubData
import zhelenskiy.ru.commands.CommandStorage
import zhelenskiy.ru.models.common.CommonModel
import zhelenskiy.ru.models.reports.ReportingSystem

data class SimpleConfiguration(
    override val mainStorage: CommandStorage<CommonModel>.ReadOnlyView =
        CommandStorage<CommonModel>().ReadOnlyView(CommonModel()).apply { addStubData() },
    override val statisticsStorage: ReportingSystem = ReportingSystem(mainStorage),
    override val clock: Clock.System = Clock.System,
) : Configuration