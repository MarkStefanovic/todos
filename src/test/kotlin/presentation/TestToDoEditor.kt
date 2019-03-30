package presentation

import app.AppScope
import app.Token
import domain.DisplayArea
import domain.ToDo
import framework.Identifier
import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import tornadofx.*
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

    private fun openEditor(mode: EditorMode, todo: ToDo, displayArea: DisplayArea) {
        view = find(
            type = ToDoEditor::class,
            scope = scope,
            params = mapOf(
                "mode" to mode,
                "todo" to todo,
                "displayArea" to displayArea,
                "token" to Token.Reminder
            )
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
        openEditor(mode = EditorMode.Add, todo = todo, displayArea = DisplayArea.Reminders)
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
        openEditor(mode = EditorMode.Edit, todo = todo, displayArea = DisplayArea.Reminders)
        assertEquals(view.title, "Edit Reminders")
    }

    @Test
    fun `correct defaults on Add mode`() {
        openEditor(mode = EditorMode.Add, todo = ToDo.default(), displayArea = DisplayArea.Reminders)
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
        openEditor(mode = EditorMode.Edit, todo = todo, displayArea = DisplayArea.Reminders)
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
        openEditor(mode = EditorMode.Edit, todo = todo, displayArea = DisplayArea.Reminders)
        val observer = TestObserver<Pair<Identifier, ToDo>>()
        scope.todoEventModel.updateResponse.subscribe(observer)
        clickOn(view.saveButton)
        observer.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 500)
        observer.assertValue(Token.Reminder to todo)
    }
}