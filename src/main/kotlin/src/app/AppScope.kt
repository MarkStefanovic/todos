package src.app

import src.controller.BaseController
import src.model.ToDo
import tornadofx.*


class AppScope(
    val toDoController : BaseController<ToDo>,
    val reminderController : BaseController<ToDo>
) : Scope()
