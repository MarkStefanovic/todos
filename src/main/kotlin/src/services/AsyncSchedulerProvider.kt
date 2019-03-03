package src.services

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import src.framework.SchedulerProvider


class AsyncSchedulerProvider : SchedulerProvider {
    override fun computation() = Schedulers.computation()
    override fun ui() = JavaFxScheduler.platform()
    override fun io() = Schedulers.io()
}