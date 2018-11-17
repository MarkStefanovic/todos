package view

import helpers.MockToDoController
import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import src.app.AppScope
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
        val testObserver = TestObserver<List<ToDo>>()
        testController.refreshResponse.subscribe(testObserver)
        testController.refreshRequest.onNext(Unit)
        testObserver.awaitCount(1)

        val expectedRows = listView.table.items.count()
        Assertions.assertEquals(expectedRows, 3)
    }

    @Test
    fun `delete button should delete selected item`() {
        val testObserver = TestObserver<Int>()
        testController.deleteResponse.subscribe(testObserver)

        testController.refresh()
        listView.table.selectWhere { it.id == 2 }
        Platform.runLater {
            listView.deleteButton.fire()
        }
        // wait for delete confirmation dialog to open
        Thread.sleep(100)

        Assertions.assertNotNull(
            listView.deleteConfirmation,
            "A confirmation dialog should have opened to confirm deletion."
        )
        Platform.runLater {
            val okButton = listView.deleteConfirmation!!.dialogPane.lookupButton(ButtonType.YES) as Button
            okButton.fire()
        }
        testObserver.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 1000)
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
