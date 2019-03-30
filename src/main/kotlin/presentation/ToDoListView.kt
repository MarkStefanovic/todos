package presentation

import app.AppScope
import app.Token
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import domain.DisplayArea
import domain.ToDo
import domain.ToDo.Companion.NONE_DATE
import framework.Identifier
import io.reactivex.rxkotlin.subscribeBy
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.stage.Modality
import mu.KLogging
import presentation.Styles.Companion.centerAlignedCell
import tornadofx.*
import java.time.LocalDate


class ToDoListView : Fragment() {
    override val scope = super.scope as AppScope

    companion object : KLogging()

    private val token: Identifier by param()

    private val displayArea =
        when (token) {
            is Token.Reminder -> DisplayArea.Reminders
            is Token.ToDo -> DisplayArea.ToDos
            else -> throw Exception("Invalid token, $token, passed to the the ToDoList view.")
        }

    private var table: TableView<ToDo> by singleAssign()

    val todayOnly = SimpleBooleanProperty(true).apply {
        onChange {
            scope.todoEventModel.refreshRequest.onNext(token)
        }
    }

    private val filter = SimpleStringProperty("").apply {
        onChange { description ->
            if (description.isNullOrBlank())
                scope.todoEventModel.refreshRequest.onNext(token)
            else
                scope.todoEventModel.filterRequest.onNext(
                    token to { todo: ToDo ->
                        description.toLowerCase() in todo.description.toLowerCase()
                    }
                )
        }
        scope.todoEventModel.refreshResponse.subscribe { value = "" }

    }

    init {
        title = displayArea.name
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
                                    scope.todoEventModel.updateRequest.onNext(token to updatedToDo)
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

                scope.todoEventModel.addResponse.subscribeBy(
                    onNext = { (_, todo) ->
                        if (todayOnly.value) {
                            if (todo.display) items.add(todo)
                        } else {
                            items.add(todo)
                        }
                    },
                    onError = scope.alertService::alertError
                )
                fun updateItems(todos: List<ToDo>) {
                    val newToDos =
                        if (todayOnly.value) {
                            todos.filter { todo -> todo.display }
                        } else {
                            todos
                        }.filter { todo -> todo.displayArea == displayArea }
                    val prior = this@tableview.items.toSet()
                    val new = newToDos.toSet()
                    val changes = (new - prior) + (prior - new)
                    if (changes.count() > 0) {
                        items.setAll(newToDos)
                    }
                }
                scope.todoEventModel.refreshResponse.subscribeBy(
                    onNext = { (_, todos) ->
                        updateItems(todos = todos)
                    },
                    onError = scope.alertService::alertError
                )
                scope.todoEventModel.filterResponse.subscribeBy(
                    onNext = { (_, todos) ->
                        updateItems(todos = todos)
                    },
                    onError = scope.alertService::alertError
                )
                scope.todoEventModel.deleteResponse.subscribeBy(
                    onNext = { (_, todo) -> items.removeIf { it.id == todo.id } },
                    onError = scope.alertService::alertError
                )
                scope.todoEventModel.updateResponse.subscribeBy(
                    onNext = { (_, todo) ->
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
                    onError = scope.alertService::alertError
                )
                onSelectionChange { it?.let { scope.todoSelected.onNext(it) } }
            }
        }

        top {
            toolbar {
                button {
                    id = "add-button"
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Add item")
                    action {
                        find(
                            type = ToDoEditor::class,
                            scope = scope,
                            params = mapOf(
                                "mode" to EditorMode.Add,
                                "todo" to ToDo.default(),
                                "displayArea" to displayArea,
                                "token" to token
                            )
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
                    tooltip("Delete selected item")
                    action {
                        table.selectionModel.selectedItem?.let { todo ->
                            if (scope.confirmationService.confirm("Are you sure you want to delete '${todo.description}'?"))
                                scope.todoEventModel.deleteRequest.onNext(token to todo)
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                button {
                    id = "refresh-button"
                    graphic = FontAwesomeIconView(FontAwesomeIcon.REFRESH).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Refresh table")
                    action { scope.todoEventModel.refreshRequest.onNext(token) }
                }
                button {
                    id = "edit-button"
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Edit item")
                    action {
                        table.selectionModel.selectedItem?.let { todo ->
                            find(
                                type = ToDoEditor::class,
                                scope = scope,
                                params = mapOf(
                                    "mode" to EditorMode.Edit,
                                    "todo" to todo,
                                    "displayArea" to displayArea,
                                    "token" to token
                                )
                            ).openModal(
                                primaryStage.style,
                                Modality.WINDOW_MODAL
                            )
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                textfield(filter) { id = "filter-text" }
                checkbox("Today", todayOnly)
            }
        }
    }
}