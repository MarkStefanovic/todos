package presentation

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import domain.DisplayArea
import domain.ToDo
import domain.ToDo.Companion.NONE_DATE
import framework.AlertService
import framework.ConfirmationService
import framework.RepositoryEventModel
import io.reactivex.rxkotlin.subscribeBy
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.stage.Modality
import mu.KLogging
import presentation.Styles.Companion.centerAlignedCell
import tornadofx.*
import java.time.LocalDate


class ToDoListView(
    private val alertService: AlertService,
    private val confirmationService: ConfirmationService,
    private val displayArea: DisplayArea,
    private val eventModel: RepositoryEventModel<ToDo>
) : Fragment() {

    companion object : KLogging()

    private var table: TableView<ToDo> by singleAssign()
    private var descriptionFilter: TextField by singleAssign()

    val todayOnly = SimpleBooleanProperty(true).apply {
        onChange {
            descriptionFilter.text = ""
            eventModel.refreshRequest.onNext(Unit)
        }
    }

    override val root = borderpane {
        paddingAll = 4

        center {
            table = tableview {
                id = "table"

                readonlyColumn("Description", ToDo::description) {
                    prefWidth = 300.0
                }
                readonlyColumn("Frequency", ToDo::frequency) {
                    addClass(centerAlignedCell)
                }
                readonlyColumn("Days", ToDo::daysUntil) {
                    addClass(centerAlignedCell)
                }
                readonlyColumn("Due", ToDo::nextDate) {
                    addClass(centerAlignedCell)
                }
                readonlyColumn("Complete", ToDo::item) {
                    cellFormat { todo ->
                        graphic = hbox {
                            alignment = Pos.CENTER_RIGHT
                            checkbox("", todo.complete.toProperty()) {
                                action {
                                    val dateCompleted =
                                        if (todo.complete)
                                            NONE_DATE
                                        else
                                            LocalDate.now()
                                    val updatedToDo = todo.copy(dateCompleted = dateCompleted)
                                    eventModel.updateRequest.onNext(updatedToDo)
                                }
                            }
                        }
                    }
                }

                rowExpander { todo ->
                    prefHeight = 95.0
                    textarea(todo.note).apply {
                        isEditable = false
                        expandedProperty().onChange { extended ->
                            if (extended) this.text = todo.note
                        }
                    }
                }

                onSelectionChange { it?.let { selected -> eventModel.itemSelected.onNext(selected) } }
            }
        }

        top {
            toolbar {
                button {
                    id = "add-button"
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                        glyphSize = 14.0
                    }
                    val hotkey = if (displayArea == DisplayArea.ToDos) "ctrl+a" else "alt+a"
                    tooltip("Add item ($hotkey)")
                    shortcut(hotkey)
                    action {
                        ToDoEditor(
                            eventModel = eventModel,
                            mode = EditorMode.Add,
                            todo = ToDo.default(),
                            displayArea = displayArea
                        ).openModal(
                            primaryStage.style,
                            Modality.WINDOW_MODAL
                        )
                    }
                }
                button {
                    id = "delete-button"
                    graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH_ALT).apply {
                        glyphSize = 14.0
                    }
                    val hotkey = if (displayArea == DisplayArea.ToDos) "ctrl+x" else "alt+x"
                    tooltip("Delete selected item ($hotkey)")
                    shortcut(hotkey)
                    action {
                        table.selectionModel.selectedItem?.let { todo ->
                            if (confirmationService.confirm("Are you sure you want to delete '${todo.description}'?"))
                                eventModel.deleteRequest.onNext(todo)
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                button {
                    id = "refresh-button"
                    graphic = FontAwesomeIconView(FontAwesomeIcon.REFRESH).apply {
                        glyphSize = 14.0
                    }
                    val hotkey = if (displayArea == DisplayArea.ToDos) "ctrl+r" else "alt+r"
                    tooltip("Refresh ($hotkey)")
                    shortcut(hotkey)
                    action { eventModel.refreshRequest.onNext(Unit) }
                }
                button {
                    id = "edit-button"
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT).apply {
                        glyphSize = 14.0
                    }
                    val hotkey = if (displayArea == DisplayArea.ToDos) "ctrl+e" else "alt+e"
                    tooltip("Edit item ($hotkey)")
                    shortcut(hotkey)
                    action {
                        table.selectionModel.selectedItem?.let { todo ->
                            ToDoEditor(
                                eventModel = eventModel,
                                mode = EditorMode.Edit,
                                todo = todo,
                                displayArea = displayArea
                            ).openModal(
                                primaryStage.style,
                                Modality.WINDOW_MODAL
                            )
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                descriptionFilter = textfield("") {
                    id = "filter-text"
                    textProperty().addListener { observable, oldValue, newValue ->
                        if (newValue.isNullOrBlank())
                            eventModel.refreshRequest.onNext(Unit)
                        else
                            eventModel.filterRequest.onNext { todo: ToDo ->
                                newValue.toLowerCase() in todo.description.toLowerCase()
                            }
                    }
                }
                checkbox("Today", todayOnly)
            }
        }

        eventModel.addResponse.subscribeBy(
            onNext = { todo ->
                if (todayOnly.value) {
                    if (todo.display)
                        table.items.add(todo)
                } else {
                    table.items.add(todo)
                }
            },
            onError = alertService::alertError
        )
        fun updateItems(todos: List<ToDo>) {
            val newToDos =
                if (todayOnly.value) {
                    todos.filter { todo -> todo.display }
                } else {
                    todos
                }.filter { todo -> todo.displayArea == displayArea }
            val prior = table.items.toSet()
            val new = newToDos.toSet()
            val changes = (new - prior) + (prior - new)
            if (changes.count() > 0) {
                table.items.setAll(newToDos)
            }
        }
        eventModel.refreshResponse.subscribeBy(
            onNext = { todos ->
                updateItems(todos = todos)
            },
            onError = alertService::alertError
        )
        eventModel.filterResponse.subscribeBy(
            onNext = { todos ->
                updateItems(todos = todos)
            },
            onError = alertService::alertError
        )
        eventModel.deleteResponse.subscribeBy(
            onNext = { todo ->
                table.items.removeIf { it.id == todo.id }
            },
            onError = alertService::alertError
        )
        fun updateItem(todo: ToDo) {
            val ix = table.items.indexOfFirst { it.id == todo.id }
            if (ix == -1)
                throw Exception("The updated item $todo could not be found on the table.")
            else
                table.items[ix] = todo
        }
        eventModel.updateResponse.subscribeBy(
            onNext = { todo ->
                if (todayOnly.value) {
                    if (todo.display) {
                        updateItem(todo)
                    } else {
                        table.items.removeIf { it.id == todo.id }
                    }
                } else {
                    updateItem(todo)
                }
            },
            onError = alertService::alertError
        )
    }
}