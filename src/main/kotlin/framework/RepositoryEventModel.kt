package framework

import io.reactivex.subjects.PublishSubject


class RepositoryEventModel<T>(val schedulerProvider: SchedulerProvider) {
    val itemSelected: PublishSubject<T> = PublishSubject.create<T>()

    val refreshRequest: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val refreshResponse: PublishSubject<List<T>> = PublishSubject.create<List<T>>().apply {
        observeOn(schedulerProvider.ui())
    }
    val addRequest: PublishSubject<T> = PublishSubject.create<T>()
    val addResponse: PublishSubject<T> = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val deleteRequest: PublishSubject<T> = PublishSubject.create<T>()
    val deleteResponse: PublishSubject<T> = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val updateRequest: PublishSubject<T> = PublishSubject.create<T>()
    val updateResponse: PublishSubject<T> = PublishSubject.create<T>().apply {
        observeOn(schedulerProvider.ui())
    }
    val filterRequest: PublishSubject<(T) -> Boolean> = PublishSubject.create<(T) -> Boolean>()
    val filterResponse: PublishSubject<List<T>> = PublishSubject.create<List<T>>().apply {
        observeOn(schedulerProvider.ui())
    }
}
