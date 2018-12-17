package src.controller

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import mu.KLogging
import org.jetbrains.exposed.sql.Query
import tornadofx.*
import java.util.concurrent.TimeUnit


abstract class BaseController<T: Any>(schedulerProvider: BaseSchedulerProvider) : Controller() {
    companion object : KLogging()

    val refreshRequest = PublishSubject.create<String>()
    val refreshResponse = PublishSubject.create<Pair<String, List<T>>>().apply {
        observeOn(schedulerProvider.ui())
    }
    val addRequest = PublishSubject.create<T>()
    val addResponse = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val deleteRequest = PublishSubject.create<Int>()
    val deleteResponse = PublishSubject.create<Int>().apply {
        observeOn(schedulerProvider.ui())
    }
    val updateRequest = PublishSubject.create<T>()
    val updateResponse = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val filterRequest = PublishSubject.create<Pair<String, Query>>()
    val filterResponse = PublishSubject.create<Pair<String, List<T>>>().apply {
        observeOn(schedulerProvider.ui())
    }

    init {
        addRequest.subscribeOn(schedulerProvider.io()).subscribeBy (
            onNext = ::add,
            onError = { it.printStackTrace() }
        )
        deleteRequest.subscribeOn(schedulerProvider.io()).subscribeBy (
            onNext = ::delete,
            onError = { logger.error(it.message) }
        )
        refreshRequest.subscribeOn(schedulerProvider.io()).debounce(200, TimeUnit.MILLISECONDS).subscribeBy (
            onNext = ::refresh,
            onError = { logger.error(it.message) }
        )
        updateRequest.subscribeOn(schedulerProvider.io()).subscribeBy (
            onNext = ::update,
            onError = { logger.error(it.message) }
        )
        filterRequest.subscribeOn(schedulerProvider.io()).debounce(200, TimeUnit.MILLISECONDS).subscribeBy (
            onNext = ::filter,
            onError = { logger.error(it.message) }
        )
    }

    abstract fun add(newItem: T)

    abstract fun delete(id: Int)

    abstract fun update(item: T)

    abstract fun refresh(token: String)

    abstract fun filter(request: Pair<String, Query>)
}