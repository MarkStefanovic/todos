package view

import helpers.MockToDoController
import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import javafx.application.Platform
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import src.app.AppScope
import src.controller.SignalSource
import src.model.ToDo
import src.view.ToDoListView
import tornadofx.*


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // junit-platform.properties does this for us
class TestToDoListView {
    private val stage = FxToolkit.registerPrimaryStage()
    private val testController = MockToDoController()
    private val appScope = AppScope(toDoController = testController)
    private val listView = find(ToDoListView::class, appScope)

    @Test
    fun `title should be ToDos`() {
        Assertions.assertEquals("ToDos", listView.title)
    }

    @Test
    fun `refresh should display initial rows`() {
        listView.todayOnly.value = false
        val testObserver = TestObserver<Pair<SignalSource, List<ToDo>>>()
        testController.refreshResponse.subscribe(testObserver)
        testController.refreshRequest.onNext(SignalSource.TODO_LIST_VIEW)
        testObserver.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 1000)
        Assertions.assertEquals(testController.todos.count(), listView.table.items.count())
    }

    @Test
    fun `delete button should delete selected item`() {
        listView.todayOnly.value = false

        val testObserver = TestObserver<Int>()
        testController.deleteResponse.subscribe(testObserver)

        testController.refresh(SignalSource.TODO_LIST_VIEW)
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
