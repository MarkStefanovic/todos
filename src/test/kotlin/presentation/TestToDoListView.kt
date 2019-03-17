package presentation

import dummies.*
import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import src.app.AppScope
import src.app.Token
import src.domain.ToDo
import src.framework.Identifier
import src.framework.RepositoryController
import src.presentation.ToDoListView
import tornadofx.*


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // junit-platform.properties does this for us
class TestToDoListView : ApplicationTest() {
    private lateinit var stage: Stage
    private lateinit var scope: AppScope
    private lateinit var view: ToDoListView
    private lateinit var addButton: Button
    private lateinit var deleteButton: Button
    private lateinit var editButton: Button
    private lateinit var refreshButton: Button
    private lateinit var table: TableView<ToDo>
    private lateinit var filterTextBox: TextField

    @BeforeEach
    fun setUp() {
        stage = FxToolkit.registerPrimaryStage()
        scope = AppScope(
            todoController = RepositoryController(
                repository = DummyRepository(items = holidays.toMutableList()),
                alertService = DummyAlertService(),
                schedulerProvider = TrampolineSchedulerProvider()
            ),
            alertService = DummyAlertService(),
            confirmationService = DummyConfirmationService()
        )
        view = find(
            type = ToDoListView::class,
            scope = scope,
            params = mapOf("token" to Token.ReminderListView)
        )
        interact {
            stage.scene = Scene(view.root)
            stage.show()
            stage.toFront()
        }
        addButton = from(view.root).lookup("#add-button").query()
        deleteButton = from(view.root).lookup("#delete-button").query()
        editButton = from(view.root).lookup("#edit-button").query()
        refreshButton = from(view.root).lookup("#refresh-button").query()
        table = from(view.root).lookup("#table").query()
        filterTextBox = from(view.root).lookup("#filter-text").query()
    }

    private fun refresh() {
        val observer = TestObserver<Pair<Identifier, List<ToDo>>>()
        scope.todoController.refreshResponse.subscribe(observer)

        view.todayOnly.value = false
        clickOn(refreshButton)

        observer.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 1000)
    }

    @Test
    fun `test initial state of view`() {
        assertTrue(!refreshButton.isDisabled)
        assertTrue(editButton.isDisabled)
        assertTrue(deleteButton.isDisabled)
        assertEquals("", filterTextBox.text)
    }

    @Test
    fun `when today checkbox off refresh should display all rows`() {
        refresh()
        Assertions.assertEquals(7, table.items.count())
    }

    @Test
    fun `delete button should delete selected item`() {
        refresh()
        Platform.runLater {
            table.selectWhere { it.id == 2 }
            Assertions.assertEquals(2, table.selectionModel.selectedItem.id)
        }
        clickOn(deleteButton)
        Assertions.assertNull(table.items.find { it.id == 2 })
        Assertions.assertEquals(6, table.items.count())
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
