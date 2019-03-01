package src.controller

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import mu.KLogging
import org.jetbrains.exposed.sql.Query
import src.view.AlertService
import tornadofx.*
import java.util.concurrent.TimeUnit


enum class Token {
    REMINDER_EDITOR,
    REMINDER_LIST_VIEW,
    TODO_EDITOR,
    TODO_LIST_VIEW,
}

abstract class BaseController<T : Any>(
    alertService: AlertService,
    schedulerProvider: BaseSchedulerProvider
) : Controller() {
    companion object : KLogging()

    val refreshRequest = PublishSubject.create<Token>()
    val refreshResponse = PublishSubject.create<Pair<Token, List<T>>>().apply {
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
    val filterRequest = PublishSubject.create<Pair<Token, Query>>()
    val filterResponse = PublishSubject.create<Pair<Token, List<T>>>().apply {
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

    abstract fun add(newItem: T)

    abstract fun delete(id: Int)

    abstract fun update(item: T)

    abstract fun refresh(token: Token)

    abstract fun filter(request: Pair<Token, Query>)
}