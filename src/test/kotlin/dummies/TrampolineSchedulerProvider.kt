package dummies

import framework.SchedulerProvider
import io.reactivex.schedulers.Schedulers

class TrampolineSchedulerProvider : SchedulerProvider {
    override fun computation() = Schedulers.trampoline()
    override fun ui() = Schedulers.trampoline()
    override fun io() = Schedulers.trampoline()
}