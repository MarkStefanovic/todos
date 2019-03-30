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
            onNext = ::refresh,
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

    open fun add(request: Pair<Identifier, T>) {
        val (token, item) = request
        repository.add(item)?.let { newItem ->
            eventModel.addResponse.onNext(token to newItem)
        }
    }

    open fun delete(request: Pair<Identifier, T>) {
        val (token, item) = request
        repository.delete(item)?.let { deletedId ->
            eventModel.deleteResponse.onNext(token to item)
        }
    }

    open fun refresh(token: Identifier) {
        repository.all()?.let { items ->
            eventModel.refreshResponse.onNext(token to items)
        }
    }

    open fun update(request: Pair<Identifier, T>) {
        val (token, item) = request
        repository.update(item)?.let { updatedItem ->
            eventModel.updateResponse.onNext(token to updatedItem)
        }
    }

    open fun filter(request: Pair<Identifier, (T) -> Boolean>) {
        val (token, criteria) = request
        repository.filter(criteria)?.let { items ->
            eventModel.filterResponse.onNext(token to items)
        }
    }
}