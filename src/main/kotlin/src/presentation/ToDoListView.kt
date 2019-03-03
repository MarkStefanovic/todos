package src.presentation

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.reactivex.rxkotlin.subscribeBy
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.stage.Modality
import mu.KLogging
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import src.app.AppScope
import src.domain.DisplayArea
import src.domain.ToDo
import src.domain.ToDo.Companion.NONE_DATE
import src.domain.ToDos
import src.framework.AlertService
import src.framework.ConfirmationService
import src.framework.EventModel
import src.framework.Token
import src.presentation.Styles.Companion.centerAlignedCell
import tornadofx.*
import java.time.LocalDate


class ToDoListView(
    private val eventModel: EventModel<ToDo>,
    private val token: Token,
    private val alertService: AlertService,
    private val confirmationService: ConfirmationService
) : View() {

    override val scope = super.scope as AppScope

    companion object : KLogging()

    private val displayArea =
        when (token) {
            Token.ReminderListView -> DisplayArea.Reminders
            Token.ToDoListView -> DisplayArea.ToDos
            else -> throw Exception("Invalid token, $token, passed to the the ToDoList view.")
        }

    // Control handles for testing
    var table: TableView<ToDo> by singleAssign()
    var addButton: Button by singleAssign()
    var deleteButton: Button by singleAssign()
    var refreshButton: Button by singleAssign()
    var editButton: Button by singleAssign()
    var filterText: TextField by singleAssign()
    var todayCheckbox: CheckBox by singleAssign()

    val todayOnly = SimpleBooleanProperty(true).apply {
        onChange {
            eventModel.refreshRequest.onNext(token)
        }
    }

    private val filter = SimpleStringProperty().apply {
        onChange { description ->
            if (description.isNullOrBlank())
                eventModel.refreshRequest.onNext(token)
            else
                eventModel.filterRequest.onNext(
                    token to ToDos.select {
                        ToDos.description.lowerCase() like "%${description.toLowerCase()}%"
                    }
                )
        }
        eventModel.refreshResponse.subscribe { value = "" }
    }

    init {
        title = displayArea.name
    }

    override val root = borderpane {
        paddingAll = 4

        center {
            table = tableview {
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

                eventModel.addResponse.subscribeBy(
                    onNext = {
                        if (todayOnly.value) {
                            if (it.display) items.add(it)
                        } else {
                            items.add(it)
                        }
                    },
                    onError = alertService::alertError
                )
                fun updateItems(source: Token, todos: List<ToDo>) {
                    if (source == token) {
                        val ts =
                            if (todayOnly.value) {
                                todos.filter { todo ->
                                    todo.display
                                }
                            } else {
                                todos
                            }.filter { todo ->
                                todo.displayArea == displayArea
                            }
                        items.setAll(ts)
                    }
                }
                eventModel.refreshResponse.subscribeBy(
                    onNext = { (source, todos) ->
                        updateItems(source = source, todos = todos)
                    },
                    onError = alertService::alertError
                )
                eventModel.filterResponse.subscribeBy(
                    onNext = { (source, todos) ->
                        updateItems(source = source, todos = todos)
                    },
                    onError = alertService::alertError
                )
                eventModel.deleteResponse.subscribeBy(
                    onNext = { id -> items.removeIf { it.id == id } },
                    onError = alertService::alertError
                )
                eventModel.updateResponse.subscribeBy(
                    onNext = { todo ->
                        if (todayOnly.value) {
                            if (todo.display) {
                                items[items.indexOfFirst { it.id == todo.id }] = todo
                            } else {
                                items.removeIf { it.id == todo.id }
                            }
                        } else {
                            items[items.indexOfFirst { it.id == todo.id }] = todo
                        }
                    },
                    onError = alertService::alertError
                )
            }
        }

        top {
            toolbar {
                addButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Add item")
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
                deleteButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH_ALT).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Delete selected item")
                    action {
                        table.selectionModel.selectedItem?.let { todo ->
                            if (confirmationService.confirm("Are you sure you want to delete '${todo.description}'?"))
                                eventModel.deleteRequest.onNext(todo.id)
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                refreshButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.REFRESH).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Refresh table")
                    action { eventModel.refreshRequest.onNext(token) }
                }
                editButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Edit item")
                    action {
                        table.selectionModel.selectedItem?.let {
                            ToDoEditor(
                                eventModel = eventModel,
                                mode = EditorMode.Edit,
                                todo = it,
                                displayArea = displayArea
                            ).openModal(
                                primaryStage.style,
                                Modality.WINDOW_MODAL
                            )
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                filterText = textfield(filter)
                todayCheckbox = checkbox("Today", todayOnly)
            }
        }
    }
}