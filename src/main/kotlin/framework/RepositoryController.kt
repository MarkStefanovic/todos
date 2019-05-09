package framework

import io.reactivex.rxkotlin.subscribeBy
import mu.KLogging
import java.util.concurrent.TimeUnit

open class RepositoryController<T : Any>(
    val eventModel: RepositoryEventModel<T>,
    val repository: Repository<T>,
    val alertService: AlertService,
    val schedulerProvider: SchedulerProvider
) {
    companion object : KLogging()

    open fun start() {
        eventModel.addRequest.subscribeOn(schedulerProvider.io()).subscribeBy(
            onNext = ::add,
            onError = alertService::alertError
        )
        eventModel.deleteRequest.subscribeOn(schedulerProvider.io()).subscribeBy(
            onNext = ::delete,
            onError = alertService::alertError
        )
        eventModel.refreshRequest.subscribeOn(schedulerProvider.io()).debounce(200, TimeUnit.MILLISECONDS).subscribeBy(
            onNext = { refresh() },
            onError = alertService::alertError
        )
        eventModel.updateRequest.subscribeOn(schedulerProvider.io()).subscribeBy(
            onNext = ::update,
            onError = alertService::alertError
        )
        eventModel.filterRequest.subscribeOn(schedulerProvider.io()).debounce(200, TimeUnit.MILLISECONDS).subscribeBy(
            onNext = ::filter,
            onError = alertService::alertError
        )
    }

    open fun add(item: T) {
        repository.add(item)?.let { newItem ->
            eventModel.addResponse.onNext(newItem)
        }
    }

    open fun delete(item: T) {
        repository.delete(item)?.let {
            eventModel.deleteResponse.onNext(item)
        }
    }

    open fun refresh() {
        repository.all().let { items ->
            eventModel.refreshResponse.onNext(items)
        }
    }

    open fun update(item: T) {
        repository.update(item)?.let { updatedItem ->
            eventModel.updateResponse.onNext(updatedItem)
        }
    }

    open fun filter(predicate: (T) -> Boolean) {
        repository.filter(predicate).let { items ->
            eventModel.filterResponse.onNext(items)
        }
    }
}