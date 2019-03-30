package app

import domain.ToDo
import framework.AlertService
import framework.ConfirmationService
import framework.RepositoryEventModel
import io.reactivex.subjects.PublishSubject
import tornadofx.*


class AppScope(
    val todoEventModel: RepositoryEventModel<ToDo>,
    val alertService: AlertService,
    val confirmationService: ConfirmationService
) : Scope() {
    val todoSelected = PublishSubject.create<ToDo>()
}
