package presentation

import app.AppScope
import domain.DisplayArea
import domain.ToDo
import domain.ToDoController
import dummies.DummyAlertService
import dummies.DummyConfirmationService
import dummies.DummyRepository
import dummies.TrampolineSchedulerProvider
import framework.RepositoryEventModel
import java.time.DayOfWeek


fun setUpTestScope(): AppScope {
    val alertService = DummyAlertService()
    val repository = DummyRepository(
        items = mutableListOf(
            ToDo.default().copy(
                id = 1,
                description = "Thanksgiving",
                frequency = "Irregular",
                month = 11,
                weekNumber = 4,
                weekday = DayOfWeek.THURSDAY.value,
                advanceNotice = 14,
                expireDays = 3,
                displayArea = DisplayArea.Reminders
            ),
            ToDo.default().copy(
                id = 2, description = "Christmas", frequency = "Yearly", month = 12, monthday = 25, advanceNotice = 14,
                expireDays = 3, displayArea = DisplayArea.Reminders
            ),
            ToDo.default().copy(
                id = 3,
                description = "Fathers Day",
                frequency = "Irregular",
                month = 6,
                weekNumber = 3,
                weekday = DayOfWeek.SUNDAY.value,
                advanceNotice = 14,
                expireDays = 3,
                displayArea = DisplayArea.Reminders
            ),
            ToDo.default().copy(
                id = 4,
                description = "Mothers Day",
                frequency = "Irregular",
                month = 5,
                weekNumber = 2,
                weekday = DayOfWeek.SUNDAY.value,
                advanceNotice = 14,
                expireDays = 3,
                displayArea = DisplayArea.Reminders
            ),
            ToDo.default().copy(
                id = 5,
                description = "Labor Day",
                frequency = "Irregular",
                month = 9,
                weekNumber = 1,
                weekday = DayOfWeek.MONDAY.value,
                advanceNotice = 14,
                expireDays = 3,
                displayArea = DisplayArea.Reminders
            ),
            ToDo.default().copy(
                id = 6, description = "New Year's", frequency = "Yearly", month = 1, monthday = 1, advanceNotice = 14,
                expireDays = 3, displayArea = DisplayArea.Reminders
            ),
            ToDo.default().copy(
                id = 7, description = "Easter", frequency = "Easter", advanceNotice = 14, expireDays = 3,
                displayArea = DisplayArea.Reminders
            )
        )
    )
    val scheduler = TrampolineSchedulerProvider()
    val eventModel = RepositoryEventModel<ToDo>(schedulerProvider = scheduler)
    val scope = AppScope(
        todoEventModel = eventModel,
        alertService = DummyAlertService(),
        confirmationService = DummyConfirmationService()
    )
    val controller = ToDoController(
        alertService = alertService,
        eventModel = eventModel,
        repository = repository,
        schedulerProvider = scheduler
    )
    controller.start()
    return scope
}