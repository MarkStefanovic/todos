package services

import framework.SchedulerProvider
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers


class AsyncSchedulerProvider : SchedulerProvider {
    override fun computation() = Schedulers.computation()
    override fun ui() = JavaFxScheduler.platform()
    override fun io() = Schedulers.io()
}