package presentation

import app.AppScope
import domain.DisplayArea
import domain.ToDo
import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import java.time.LocalDate


class TestToDoEditor : ApplicationTest() {
    private val today = LocalDate.now()

    private lateinit var stage: Stage
    private lateinit var scope: AppScope
    private lateinit var view: ToDoEditor

    @BeforeEach
    fun setUp() {
        stage = FxToolkit.registerPrimaryStage()
        scope = setUpTestScope()
    }

    private fun openEditor(mode: EditorMode, todo: ToDo) {
        view = ToDoEditor(
            eventModel = scope.reminderEventModel,
            mode = mode,
            todo = todo,
            displayArea = DisplayArea.Reminders
        )
        interact {
            stage.scene = Scene(view.root)
            stage.show()
            stage.toFront()
        }
    }

    @Test
    fun `title is 'Add Reminders' when mode is 'Add' and displayArea is 'Reminders'`() {
        val todo = ToDo.default().copy(
            description = "Test",
            frequency = "Yearly",
            month = 10,
            monthday = 12,
            displayArea = DisplayArea.Reminders
        )
        openEditor(mode = EditorMode.Add, todo = todo)
        assertEquals(view.title, "Add New Reminders")
    }

    @Test
    fun `title is 'Edit Reminders' when mode is 'Edit' and displayArea is 'Reminders'`() {
        val todo = ToDo.default().copy(
            description = "Test",
            frequency = "Yearly",
            month = 10,
            monthday = 12,
            displayArea = DisplayArea.Reminders
        )
        openEditor(mode = EditorMode.Edit, todo = todo)
        assertEquals(view.title, "Edit Reminders")
    }

    @Test
    fun `correct defaults on Add mode`() {
        openEditor(mode = EditorMode.Add, todo = ToDo.default())
        assertEquals("Once", view.frequencyField.value)
        assertEquals(today, view.onceField.value)
        assertEquals(today.monthValue, view.monthField.value)
        assertEquals(today.dayOfMonth, view.monthdayField.value)
        assertEquals("Monday", view.weekdayField.value)
    }

    @Test
    fun `fields set on 'Edit' mode match passed todo passed in`() {
        val todo = ToDo.default().copy(
            description = "Test",
            frequency = "Yearly",
            month = 10,
            monthday = 12,
            displayArea = DisplayArea.Reminders
        )
        openEditor(mode = EditorMode.Edit, todo = todo)
        assertEquals("Test", view.descriptionField.text)
        assertEquals("Yearly", view.frequencyField.value)
        assertEquals(LocalDate.of(today.year, 10, 12), view.onceField.value)
        assertEquals(10, view.monthField.value)
        assertEquals(12, view.monthdayField.value)
        assertEquals("Monday", view.weekdayField.value)
    }

    @Test
    fun `todo edited matches state of form`() {
        val todo = ToDo.default().copy(
            description = "Test",
            frequency = "Yearly",
            month = 10,
            monthday = 12,
            displayArea = DisplayArea.Reminders
        )
        openEditor(mode = EditorMode.Edit, todo = todo)
        val observer = TestObserver<ToDo>()
        scope.reminderEventModel.updateResponse.subscribe(observer)
        clickOn(view.saveButton)
        observer.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 5000)
        observer.assertValue(todo)
    }
}