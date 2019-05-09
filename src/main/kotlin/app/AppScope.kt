package app

import domain.ToDo
import framework.AlertService
import framework.ConfirmationService
import framework.RepositoryEventModel
import tornadofx.*


class AppScope(
    val todoEventModel: RepositoryEventModel<ToDo>,
    val reminderEventModel: RepositoryEventModel<ToDo>,
    val alertService: AlertService,
    val confirmationService: ConfirmationService
) : Scope()
