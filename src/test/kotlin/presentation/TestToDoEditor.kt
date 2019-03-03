package presentation

import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import junit.framework.Assert.assertEquals
import mockServices.TrampolineSchedulerProvider
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import src.domain.ToDo
import src.presentation.EditorMode
import src.presentation.ToDoEditor
import java.time.LocalDate


class TestToDoEditor {
    private val stage = FxToolkit.registerPrimaryStage()
    private val eventModel = ToDoEventModel(schedulerProvider = TrampolineSchedulerProvider())
    private val testObserver = TestObserver<ToDo>()
    private val today = LocalDate.now()
    private val yearlyToDo = ToDo.default().copy(
        description = "Test",
        frequency = "Yearly",
        month = 10,
        monthday = 12,
        displayArea = DisplayArea.Reminders
    )

    @Test
    fun `title is 'Add Reminders' when mode is 'Add' and displayArea is 'Reminders'`() {
        val editor = ToDoEditor(
            mode = EditorMode.Add,
            todo = ToDo.default(),
            displayArea = DisplayArea.Reminders,
            eventModel = eventModel
        )
        assertEquals(editor.title, "Add New Reminders")
    }

    @Test
    fun `title is 'Edit Reminders' when mode is 'Edit' and displayArea is 'Reminders'`() {
        val editor = ToDoEditor(
            mode = EditorMode.Edit,
            todo = ToDo.default(),
            displayArea = DisplayArea.Reminders,
            eventModel = eventModel
        )
        assertEquals(editor.title, "Edit Reminders")
    }

    @Test
    fun `correct defaults on Add mode`() {
        val editor = ToDoEditor(
            mode = EditorMode.Edit,
            todo = ToDo.default(),
            displayArea = DisplayArea.Reminders,
            eventModel = eventModel
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
            mode = EditorMode.Edit,
            todo = yearlyToDo,
            displayArea = DisplayArea.Reminders,
            eventModel = eventModel
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
            mode = EditorMode.Edit,
            todo = yearlyToDo,
            displayArea = DisplayArea.Reminders,
            eventModel = eventModel
        )
        eventModel.updateResponse.subscribe(testObserver)
        editor.saveButton.fire()
        testObserver.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_100MS, 500)
        testObserver.assertValue(yearlyToDo)
    }
}