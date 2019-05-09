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
    override fun update(item: ToDo) {
        if (item.frequency == "Once" && item.complete) {
            eventModel.deleteRequest.onNext(item)
        } else {
            super.update(item)
        }
    }
}
