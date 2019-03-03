package src.presentation

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.scene.control.Button
import javafx.scene.control.DatePicker
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import org.controlsfx.control.PrefixSelectionComboBox
import src.app.AppScope
import src.domain.DisplayArea
import src.domain.ToDo
import src.domain.getWeekdayByName
import src.framework.EventModel
import tornadofx.*
import java.time.LocalDate


val FREQUENCIES = listOf("Once", "Daily", "Weekly", "Monthly", "Yearly", "XDays")
val WEEKDAY_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

enum class EditorMode {
    Add, Edit
}

class ToDoEditor(
    val eventModel: EventModel<ToDo>,
    val mode: EditorMode,
    val todo: ToDo,
    displayArea: DisplayArea
) : Fragment() {

    override val scope = super.scope as AppScope

    var advanceNoticeField: PrefixSelectionComboBox<Int> by singleAssign()
    var descriptionField: TextField by singleAssign()
    var expireDaysField: PrefixSelectionComboBox<Int> by singleAssign()
    var frequencyField: PrefixSelectionComboBox<String> by singleAssign()
    var monthField: PrefixSelectionComboBox<Int> by singleAssign()
    var monthdayField: PrefixSelectionComboBox<Int> by singleAssign()
    var onceField: DatePicker by singleAssign()
    var weekdayField: PrefixSelectionComboBox<String> by singleAssign()
    var startDateField: DatePicker by singleAssign()
    var daysField: PrefixSelectionComboBox<Int> by singleAssign()
    var noteField: TextArea by singleAssign()
    var saveButton: Button by singleAssign()

    private val validationContext = ValidationContext()

    init {
        val titleBase = if (mode == EditorMode.Edit) "Edit" else "Add New"
        title = "$titleBase ${displayArea.name}"
    }

    override val root = vbox {
        paddingAll = 4.0

        form {
            minHeight = 400.0

            fieldset {
                field("Frequency") {
                    frequencyField = stringPrefixSelector(todo.frequency, FREQUENCIES)
                }
                field("Description") {
                    prefWidth = 400.0
                    descriptionField = textField(
                        validationContext = validationContext,
                        initialValue = todo.description,
                        minLength = 1,
                        maxLength = 90
                    )
                }
                field("Weekday") {
                    weekdayField = stringPrefixSelector(todo.weekdayName, WEEKDAY_NAMES)
                }.bindToChildVisibility(weekdayField)

                field("Month") {
                    maxWidth = 200.0
                    monthField = integerPrefixSelector(initialValue = todo.month, minValue = 1, maxValue = 12)
                }.bindToChildVisibility(monthField)
                field("Day of Month") {
                    maxWidth = 200.0
                    monthdayField = integerPrefixSelector(initialValue = todo.monthday, minValue = 1, maxValue = 31)
                }.bindToChildVisibility(monthdayField)
                field("Date") {
                    maxWidth = 210.0
                    val today = LocalDate.now()
                    onceField = datePicker(
                        validationContext = validationContext,
                        initialValue = todo.onceDate,
                        minDate = LocalDate.of(1900, 1, 1),
                        maxDate = LocalDate.of(today.year + 10, today.monthValue, today.dayOfMonth)
                    )
                }.bindToChildVisibility(onceField)
                field("Start Date") {
                    maxWidth = 210.0
                    val today = LocalDate.now()
                    startDateField = datePicker(
                        validationContext = validationContext,
                        initialValue = todo.startDate,
                        minDate = LocalDate.of(1900, 1, 1),
                        maxDate = LocalDate.of(today.year + 10, today.monthValue, today.dayOfMonth)
                    )
                }.bindToChildVisibility(startDateField)
                field("Days") {
                    maxWidth = 200.0
                    daysField = integerPrefixSelector(
                        initialValue = todo.days,
                        minValue = 1,
                        maxValue = 999
                    )
                }.bindToChildVisibility(daysField)
                field("Advance Notice") {
                    maxWidth = 200.0
                    advanceNoticeField = integerPrefixSelector(
                        initialValue = todo.advanceNotice,
                        minValue = 0,
                        maxValue = 999
                    )
                }
                field("Expire Days") {
                    maxWidth = 200.0
                    expireDaysField = integerPrefixSelector(
                        initialValue = todo.expireDays,
                        minValue = 0,
                        maxValue = 180
                    )
                }
                field("Note") {
                    prefWidth = 400.0
                    noteField = textarea(todo.note) {
                        prefHeight = 100.0
                        maxHeight = 300.0
                    }
                }

                frequencyField.valueProperty().onChange {
                    setAvailableFields(it)
                }
                validationContext.validate()
            }
        }

        buttonbar {
            paddingBottom = 2.0

            saveButton = button {
                text = if (mode == EditorMode.Edit) "Save" else "Add"
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply { glyphSize = 18.0 }
                isDefaultButton = true
                action {
                    val baseNewToDo = ToDo.default().copy(
                        id = todo.id,
                        description = descriptionField.text,
                        frequency = frequencyField.value,
                        displayArea = DisplayArea.valueOf(displayArea.name),
                        note = noteField.text
                    )
                    val newToDo = when (frequencyField.value) {
                        "Once" -> baseNewToDo.copy(
                            advanceNotice = advanceNoticeField.value,
                            expireDays = expireDaysField.value,
                            month = onceField.value.monthValue,
                            monthday = onceField.value.dayOfMonth,
                            year = onceField.value.year
                        )
                        "Daily" -> baseNewToDo.copy(
                            advanceNotice = 0,
                            expireDays = 1
                        )
                        "Weekly" -> baseNewToDo.copy(
                            advanceNotice = advanceNoticeField.value,
                            expireDays = expireDaysField.value,
                            weekday = getWeekdayByName(weekdayField.value)
                        )
                        "Monthly" -> baseNewToDo.copy(
                            advanceNotice = advanceNoticeField.value,
                            expireDays = expireDaysField.value,
                            monthday = monthdayField.value
                        )
                        "Yearly" -> baseNewToDo.copy(
                            advanceNotice = advanceNoticeField.value,
                            expireDays = expireDaysField.value,
                            month = monthField.value,
                            monthday = monthdayField.value
                        )
                        "XDays" -> baseNewToDo.copy(
                            advanceNotice = advanceNoticeField.value,
                            expireDays = expireDaysField.value,
                            startDate = startDateField.value,
                            days = daysField.value
                        )
                        else -> throw NotImplementedError()
                    }
                    if (mode == EditorMode.Edit) {
                        eventModel.updateRequest.onNext(newToDo)
                    } else {
                        eventModel.addRequest.onNext(newToDo)
                    }
                }
                enableWhen(validationContext.valid)
            }
        }

        setAvailableFields(frequencyField.value)

        eventModel.addResponse.subscribe { close() }
        eventModel.updateResponse.subscribe { close() }
    }

    private fun setAvailableFields(frequency: String?) {
        val disableMonthday = { monthdayField.isVisible = false }
        val enableMonthday = { monthdayField.isVisible = true }
        val disableMonth = { monthField.isVisible = false }
        val enableMonth = { monthField.isVisible = true }
        val disableWeekday = { weekdayField.isVisible = false }
        val enableWeekday = { weekdayField.isVisible = true }
        val disableOnce = { onceField.isVisible = false }
        val enableOnce = { onceField.isVisible = true }
        val disableStartDate = { startDateField.isVisible = false }
        val enableStartDate = { startDateField.isVisible = true }
        val disableDays = { daysField.isVisible = false }
        val enableDays = { daysField.isVisible = true }
        fun disableAll() {
            disableStartDate()
            disableDays()
            disableMonthday()
            disableMonth()
            disableWeekday()
            disableOnce()
        }
        when (frequency) {
            "Once" -> {
                disableAll()
                enableOnce()
            }
            "Weekly" -> {
                disableAll()
                enableWeekday()
            }
            "Monthly" -> {
                disableAll()
                enableMonthday()
            }
            "Yearly" -> {
                disableAll()
                enableMonthday()
                enableMonth()
            }
            "XDays" -> {
                disableAll()
                enableStartDate()
                enableDays()
            }
            else -> {
                disableAll()
            }
        }
    }
}
