package src.framework

import io.reactivex.subjects.PublishSubject
import org.jetbrains.exposed.sql.Query

class EventModel<T : Any>(schedulerProvider: SchedulerProvider) {

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
}