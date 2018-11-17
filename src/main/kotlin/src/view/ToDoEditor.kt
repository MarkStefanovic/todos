package src.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.scene.control.Button
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import org.controlsfx.control.PrefixSelectionComboBox
import src.app.AppScope
import src.model.ToDo
import src.model.getWeekdayByName
import tornadofx.*
import java.time.LocalDate


val FREQUENCIES = listOf("Once", "Daily", "Weekly", "Monthly", "Yearly")
val WEEKDAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

class ToDoEditor(override val scope: AppScope, val mode: String, val todo: ToDo) : Fragment() {
    var advanceNoticeField: PrefixSelectionComboBox<Int> by singleAssign()
    var descriptionField: TextField by singleAssign()
    var expireDaysField: PrefixSelectionComboBox<Int> by singleAssign()
    var frequencyField: PrefixSelectionComboBox<String> by singleAssign()
    var monthField: PrefixSelectionComboBox<Int> by singleAssign()
    var monthdayField: PrefixSelectionComboBox<Int> by singleAssign()
    var onceField: DatePicker by singleAssign()
    var weekdayField: PrefixSelectionComboBox<String> by singleAssign()
    var saveButton: Button by singleAssign()

    private val validationContext = ValidationContext()

    init {
        title = if (mode == "Edit") "Edit ToDo" else "Add New ToDo"
    }

    override val root = vbox {
        paddingAll = 4.0

        form {
            minHeight = 270.0

            fieldset {
                field("Frequency") {
                    frequencyField = stringPrefixSelector(todo.frequency, FREQUENCIES)
                }

                field("Description") {
                    descriptionField = textField(
                        validationContext = validationContext,
                        initialValue = todo.description,
                        minLength = 1,
                        maxLength = 90
                    )
                }

                field("Weekday") {
                    weekdayField = stringPrefixSelector(todo.weekdayName, WEEKDAYS)
                }.bindToChildVisibility(weekdayField)

                field("Month") {
                    maxWidth = 200.0
                    monthField = integerPrefixSelector(initialValue = todo.month, minValue = 1, maxValue = 12)
                }.bindToChildVisibility(monthField)

                field("Day of Month") {
                    maxWidth = 200.0
                    monthdayField = integerPrefixSelector(initialValue = todo.monthday, minValue = 1, maxValue = 28)
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

                frequencyField.valueProperty().onChange {
                    setAvailableFields(it)
                }
                validationContext.validate()
            }
        }

        buttonbar {
            paddingBottom = 2.0

            saveButton = button {
                text = if (mode == "Edit") "Save" else "Add"
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply { glyphSize = 18.0 }
                isDefaultButton = true
                action {
                    val newToDo = todo.copy(
                        description = descriptionField.text,
                        frequency = frequencyField.value,
                        advanceNotice = advanceNoticeField.value,
                        expireDays = expireDaysField.value,
                        month = onceField.value.monthValue,
                        monthday = onceField.value.dayOfMonth,
                        weekday = getWeekdayByName(weekdayField.value),
                        year = onceField.value.year
                    )
                    if (mode == "Edit") {
                        scope.toDoController.updateRequest.onNext(newToDo)
                    } else {
                        scope.toDoController.addRequest.onNext(newToDo)
                    }
                }
                enableWhen(validationContext.valid)
            }
        }

        setAvailableFields(frequencyField.value)

        scope.toDoController.addResponse.subscribe { close() }
        scope.toDoController.updateResponse.subscribe { close() }
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
        when (frequency) {
            "Once" -> {
                disableMonthday()
                disableMonth()
                disableWeekday()
                enableOnce()
            }
            "Weekly" -> {
                disableMonthday()
                disableMonth()
                enableWeekday()
                disableOnce()
            }
            "Monthly" -> {
                enableMonthday()
                disableMonth()
                disableWeekday()
                disableOnce()
            }
            "Yearly" -> {
                enableMonthday()
                enableMonth()
                disableWeekday()
                disableOnce()
            }
            else -> {
                disableMonthday()
                disableMonth()
                disableWeekday()
                disableOnce()
            }
        }
    }
}
