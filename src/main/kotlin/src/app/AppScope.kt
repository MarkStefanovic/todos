package src.app

import src.controller.BaseController
import src.model.ToDo
import src.view.AlertService
import src.view.ConfirmationService
import tornadofx.*


class AppScope(
    val toDoController : BaseController<ToDo>,
    val reminderController: BaseController<ToDo>,
    val alertService: AlertService,
    val confirmationService: ConfirmationService
) : Scope()
