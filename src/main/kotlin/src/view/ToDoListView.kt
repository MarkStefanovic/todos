package src.view

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
import src.app.Styles.Companion.centerAlignedCell
import src.controller.BaseController
import src.controller.Token
import src.model.ToDo
import src.model.ToDo.Companion.NONE_DATE
import src.model.ToDos
import tornadofx.*
import java.time.LocalDate


class ToDoListView(
    val controller: BaseController<ToDo>,
    val token: Token,
    val alertService: AlertService,
    val confirmationService: ConfirmationService
) : View() {

    companion object : KLogging()

    private val displayArea =
        when (token) {
            Token.REMINDER_LIST_VIEW -> "Reminders"
            Token.TODO_LIST_VIEW -> "ToDos"
            else -> throw Exception(
                "Only a REMINDER_LIST_VIEW or TODO_LIST_VIEW token is accepted by the ToDoList view."
            )
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
            controller.refreshRequest.onNext(token)
        }
    }

    private val filter = SimpleStringProperty().apply {
        onChange { description ->
            if (description.isNullOrBlank())
                controller.refreshRequest.onNext(token)
            else
                controller.filterRequest.onNext(
                    token to ToDos.select {
                        ToDos.description.lowerCase() like "%${description.toLowerCase()}%"
                    }
                )
        }
        controller.refreshResponse.subscribe { value = "" }
    }

    init {
        title = displayArea
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
                                    controller.updateRequest.onNext(updatedToDo)
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

                controller.addResponse.subscribeBy(
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
                controller.refreshResponse.subscribeBy(
                    onNext = { (source, todos) ->
                        updateItems(source = source, todos = todos)
                    },
                    onError = alertService::alertError
                )
                controller.filterResponse.subscribeBy(
                    onNext = { (source, todos) ->
                        updateItems(source = source, todos = todos)
                    },
                    onError = alertService::alertError
                )
                controller.deleteResponse.subscribeBy(
                    onNext = { id -> items.removeIf { it.id == id } },
                    onError = alertService::alertError
                )
                controller.updateResponse.subscribeBy(
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
                            controller = controller,
                            mode = "App",
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
                            controller.deleteRequest.onNext(todo.id)
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                refreshButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.REFRESH).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Refresh table")
                    action { controller.refreshRequest.onNext(token) }
                }
                editButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Edit item")
                    action {
                        table.selectionModel.selectedItem?.let {
                            ToDoEditor(
                                controller = controller,
                                mode = "Edit",
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