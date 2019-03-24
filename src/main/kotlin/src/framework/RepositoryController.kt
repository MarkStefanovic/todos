package src.framework

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import mu.KLogging
import java.util.concurrent.TimeUnit

class RepositoryController<T : Any>(
    private val repository: Repository<T>,
    alertService: AlertService,
    schedulerProvider: SchedulerProvider
) {
    companion object : KLogging()

    val refreshRequest = PublishSubject.create<Identifier>()
    val refreshResponse = PublishSubject.create<Pair<Identifier, List<T>>>().apply {
        observeOn(schedulerProvider.ui())
    }
    val addRequest = PublishSubject.create<T>()
    val addResponse = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val deleteRequest = PublishSubject.create<T>()
    val deleteResponse = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val updateRequest = PublishSubject.create<T>()
    val updateResponse = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val filterRequest = PublishSubject.create<Pair<Identifier, (T) -> Boolean>>()
    val filterResponse = PublishSubject.create<Pair<Identifier, List<T>>>().apply {
        observeOn(schedulerProvider.ui())
    }

    init {
        addRequest.subscribeOn(schedulerProvider.io()).subscribeBy(
            onNext = ::add,
            onError = alertService::alertError
        )
        deleteRequest.subscribeOn(schedulerProvider.io()).subscribeBy(
            onNext = ::delete,
            onError = alertService::alertError
        )
        refreshRequest.subscribeOn(schedulerProvider.io()).debounce(200, TimeUnit.MILLISECONDS).subscribeBy(
            onNext = ::refresh,
            onError = alertService::alertError
        )
        updateRequest.subscribeOn(schedulerProvider.io()).subscribeBy(
            onNext = ::update,
            onError = alertService::alertError
        )
        filterRequest.subscribeOn(schedulerProvider.io()).debounce(200, TimeUnit.MILLISECONDS).subscribeBy(
            onNext = ::filter,
            onError = alertService::alertError
        )
    }

    private fun add(item: T) {
        repository.add(item)?.let { newItem ->
            addResponse.onNext(newItem)
        }
    }

    private fun delete(item: T) {
        repository.delete(item)?.let { deletedId ->
            deleteResponse.onNext(item)
        }
    }

    private fun refresh(token: Identifier) {
        repository.all()?.let { items ->
            refreshResponse.onNext(token to items)
        }
    }

    private fun update(item: T) {
        repository.update(item)?.let { updatedItem ->
            updateResponse.onNext(updatedItem)
        }
    }

    private fun filter(request: Pair<Identifier, (T) -> Boolean>) {
        val (token, criteria) = request
        repository.filter(criteria)?.let { items ->
            filterResponse.onNext(token to items)
        }
    }
}
