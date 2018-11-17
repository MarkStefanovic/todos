package helpers

import io.reactivex.schedulers.Schedulers
import src.controller.BaseSchedulerProvider

class TrampolineSchedulerProvider : BaseSchedulerProvider {
    override fun computation() = Schedulers.trampoline()
    override fun ui() = Schedulers.trampoline()
    override fun io() = Schedulers.trampoline()
}