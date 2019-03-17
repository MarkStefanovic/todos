package dummies

import io.reactivex.schedulers.Schedulers
import src.framework.SchedulerProvider

class TrampolineSchedulerProvider : SchedulerProvider {
    override fun computation() = Schedulers.trampoline()
    override fun ui() = Schedulers.trampoline()
    override fun io() = Schedulers.trampoline()
}