package src.framework

import io.reactivex.rxkotlin.subscribeBy
import mu.KLogging
import org.jetbrains.exposed.sql.Query
import java.util.concurrent.TimeUnit


class EventBus<T : Any>(
    private val eventModel: EventModel<T>,
    private val repository: Repository<T>,
    alertService: AlertService,
    schedulerProvider: SchedulerProvider
) {
    companion object : KLogging()

    init {
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

    private fun add(item: T) {
        repository.add(item)?.let { newItem ->
            eventModel.addResponse.onNext(newItem)
        }
    }

    private fun delete(id: Int) {
        repository.delete(id)?.let { deletedId ->
            eventModel.deleteResponse.onNext(deletedId)
        }
    }

    private fun refresh(token: Token) {
        repository.all()?.let { items ->
            eventModel.refreshResponse.onNext(token to items)
        }
    }

    private fun update(item: T) {
        repository.update(item)?.let { updatedItem ->
            eventModel.updateResponse.onNext(updatedItem)
        }
    }

    private fun filter(request: Pair<Token, Query>) {
        val (token, query) = request
        repository.filter(query)?.let { items ->
            eventModel.filterResponse.onNext(token to items)
        }
    }
}