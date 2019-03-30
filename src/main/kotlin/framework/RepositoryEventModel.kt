package framework

import io.reactivex.subjects.PublishSubject


class RepositoryEventModel<T>(val schedulerProvider: SchedulerProvider) {
    val refreshRequest = PublishSubject.create<Identifier>()
    val refreshResponse = PublishSubject.create<Pair<Identifier, List<T>>>().apply {
        observeOn(schedulerProvider.ui())
    }
    val addRequest = PublishSubject.create<Pair<Identifier, T>>()
    val addResponse = PublishSubject.create<Pair<Identifier, T>>().apply {
        observeOn(schedulerProvider.ui())
    }
    val deleteRequest = PublishSubject.create<Pair<Identifier, T>>()
    val deleteResponse = PublishSubject.create<Pair<Identifier, T>>().apply {
        observeOn(schedulerProvider.ui())
    }
    val updateRequest = PublishSubject.create<Pair<Identifier, T>>()
    val updateResponse = PublishSubject.create<Pair<Identifier, T>>().apply {
        observeOn(schedulerProvider.ui())
    }
    val filterRequest = PublishSubject.create<Pair<Identifier, (T) -> Boolean>>()
    val filterResponse = PublishSubject.create<Pair<Identifier, List<T>>>().apply {
        observeOn(schedulerProvider.ui())
    }
}
