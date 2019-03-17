package src.app

import src.domain.ToDo
import src.framework.AlertService
import src.framework.ConfirmationService
import src.framework.RepositoryController
import tornadofx.*


class AppScope(
    val todoController: RepositoryController<ToDo>,
    val alertService: AlertService,
    val confirmationService: ConfirmationService
) : Scope()
