package presentation

import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import javafx.application.Platform
import mockServices.MockAlertService
import mockServices.MockConfirmationService
import mockServices.TrampolineSchedulerProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import src.domain.ToDo
import src.framework.Token
import src.presentation.ToDoListView
import tornadofx.*


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // junit-platform.properties does this for us
class TestToDoListView {
    private val stage = FxToolkit.registerPrimaryStage()
    private val schedulerProvider = TrampolineSchedulerProvider()
    private val eventModel = ToDoEventModel(schedulerProvider)
    private val alertService = MockAlertService()
    private val confirmationService = MockConfirmationService()
    private val listView = ToDoListView(
        token = Token.ReminderListView,
        alertService = alertService,
        eventModel = eventModel,
        confirmationService = confirmationService
    )

    @Test
    fun `refresh should display initial rows`() {
        listView.todayOnly.value = false
        val testObserver = TestObserver<Pair<Token, List<ToDo>>>()
        eventModel.refreshResponse.subscribe(testObserver)
        eventModel.refreshRequest.onNext(Token.ReminderListView)
        testObserver.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 1000)
        Assertions.assertEquals(testController.todos.count(), listView.table.items.count())
    }

    @Test
    fun `delete button should delete selected item`() {
        listView.todayOnly.value = false

        val testObserver = TestObserver<Int>()
        eventModel.deleteResponse.subscribe(testObserver)

        eventModel.refreshRequest.onNext(Token.ReminderListView)
        listView.table.selectWhere { it.id == 2 }
        Assertions.assertEquals(listView.table.selectedItem?.id, 2)
        Platform.runLater {
            listView.deleteButton.fire()
        }

        testObserver.awaitCount(1)
        testObserver.assertValue(2)
        Assertions.assertNull(listView.table.items.find { it.id == 2 })
        val expectedRows = listView.table.items.count()
        Assertions.assertEquals(expectedRows, 2)
    }

    @Test
    fun `add button with no selection should open fresh ToDoEditor`() {

    }

    @Test
    fun `add button with selection should open todo in ToDoEditor`() {

    }

    @Test
    fun `filter textbox should filter displayed todos`() {

    }
}
