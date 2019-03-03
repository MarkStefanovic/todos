package src.app

import src.domain.ToDo
import src.framework.AlertService
import src.framework.ConfirmationService
import src.framework.EventModel
import tornadofx.*


class AppScope(
    val toDoEventModel: EventModel<ToDo>,
    val alertService: AlertService,
    val confirmationService: ConfirmationService
) : Scope()
