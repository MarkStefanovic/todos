package domain

import framework.*

class ToDoController(
    alertService: AlertService,
    eventModel: RepositoryEventModel<ToDo>,
    repository: Repository<ToDo>,
    schedulerProvider: SchedulerProvider
) : RepositoryController<ToDo>(
    alertService = alertService,
    eventModel = eventModel,
    repository = repository,
    schedulerProvider = schedulerProvider
) {
    override fun update(request: Pair<Identifier, ToDo>) {
        val (token, item) = request
        if ((item.frequency == "Once") and item.complete) {
            eventModel.deleteRequest.onNext(token to item)
        } else {
            super.update(token to item)
        }
    }
}
