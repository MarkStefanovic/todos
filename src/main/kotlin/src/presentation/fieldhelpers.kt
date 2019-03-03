package src.presentation

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import org.controlsfx.control.PrefixSelectionComboBox
import tornadofx.*
import tornadofx.controlsfx.prefixselectioncombobox
import java.time.LocalDate


fun Field.bindToChildVisibility(childNode: Node) : Field {
    childNode.visibleProperty().onChange { it ->
        if (it) {
            this.isVisible = true
            isManaged = true
        } else {
            this.hide()
            isManaged = false
        }
    }
    return this
}

fun Node.datePicker(
    validationContext: ValidationContext,
    initialValue: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate
) : DatePicker {
    val valueProperty = SimpleObjectProperty<LocalDate>(initialValue)
    val picker = datepicker(valueProperty)
    validationContext.addValidator(this, valueProperty) {
        when {
            valueProperty.value < minDate -> error("Date must be >= $minDate")
            valueProperty.value > maxDate -> error("Date must be <= $maxDate")
            else -> null
        }
    }
    return picker
}

fun Node.textField(
    validationContext: ValidationContext,
    initialValue: String,
    minLength: Int,
    maxLength: Int
) : TextField {
    val valueProperty = SimpleStringProperty(initialValue)

    val textField = textfield(valueProperty)

    validationContext.addValidator(this, valueProperty) {
        when {
            valueProperty.value.length < minLength -> error("Text length must be >= $minLength")
            valueProperty.value.length > maxLength -> error("Text length must be <= $maxLength")
            else -> null
        }
    }
    return textField
}


fun Node.integerPrefixSelector(initialValue: Int, minValue: Int, maxValue: Int) : PrefixSelectionComboBox<Int> {
    val items = SimpleListProperty<Int>((minValue..maxValue).toList().observable())
    return prefixselectioncombobox<Int>(items) {
        value = initialValue
        typingDelay = 500
        isDisplayOnFocusedEnabled = false
    }
}


fun Node.stringPrefixSelector(initialValue: String, items: List<String>) : PrefixSelectionComboBox<String> {
    val itemsProperty = SimpleListProperty<String>(items.observable())
    return prefixselectioncombobox<String>(itemsProperty) {
        value = initialValue
        typingDelay = 500
        isDisplayOnFocusedEnabled = false
    }
}