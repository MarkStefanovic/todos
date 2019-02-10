package view

import helpers.MockToDoController
import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import src.app.AppScope
import src.model.ToDo
import src.view.ToDoEditor
import java.time.LocalDate


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestToDoEditor {
    private val stage = FxToolkit.registerPrimaryStage()
    private val testController = MockToDoController()
    private val testObserver = TestObserver<ToDo>()
    private val today = LocalDate.now()
    private val yearlyToDo = ToDo.default().copy(
        description = "Test",
        frequency = "Yearly",
        month = 10,
        monthday = 12,
        displayArea = "Reminders"
    )

    @Test
    fun `title is 'Add Reminders' when mode is 'Add' and displayArea is 'Reminders'`() {
        val editor = ToDoEditor(
            controller = testController,
            mode = "Add",
            todo = ToDo.default(),
            displayArea = "Reminders"
        )
        assertEquals(editor.title, "Add New Reminders")
    }

    @Test
    fun `title is 'Edit Reminders' when mode is 'Edit' and displayArea is 'Reminders'`() {
        val editor = ToDoEditor(
            controller = testController,
            mode = "Edit",
            todo = ToDo.default(),
            displayArea = "Reminders"
        )
        assertEquals(editor.title, "Edit Reminders")
    }

    @Test
    fun `correct defaults on Add mode`() {
        val editor = ToDoEditor(
            controller = testController,
            mode = "Edit",
            todo = ToDo.default(),
            displayArea = "Reminders"
        )
        assertEquals("Once", editor.frequencyField.value)
        assertEquals(today, editor.onceField.value)
        assertEquals(today.monthValue, editor.monthField.value)
        assertEquals(today.dayOfMonth, editor.monthdayField.value)
        assertEquals("Monday", editor.weekdayField.value)
    }

    @Test
    fun `fields set on 'Edit' mode match passed todo passed in`() {
        val editor = ToDoEditor(
            controller = testController,
            mode = "Edit",
            todo = yearlyToDo,
            displayArea = "Reminders"
        )
        assertEquals("Test", editor.descriptionField.text)
        assertEquals("Yearly", editor.frequencyField.value)
        assertEquals(LocalDate.of(today.year, 10, 12), editor.onceField.value)
        assertEquals(10, editor.monthField.value)
        assertEquals(12, editor.monthdayField.value)
        assertEquals("Monday", editor.weekdayField.value)
    }

    @Test
    fun `todo edited matches state of form`() {
        val editor = ToDoEditor(
            controller = testController,
            mode = "Edit",
            todo = yearlyToDo,
            displayArea = "Reminders"
        )
        testController.updateResponse.subscribe(testObserver)
        editor.saveButton.fire()
        testObserver.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 500)
        testObserver.assertValue(yearlyToDo)
    }
}