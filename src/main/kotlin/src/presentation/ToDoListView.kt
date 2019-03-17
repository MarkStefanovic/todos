package src.presentation

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.reactivex.rxkotlin.subscribeBy
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.stage.Modality
import mu.KLogging
import src.app.AppScope
import src.app.Token
import src.domain.DisplayArea
import src.domain.ToDo
import src.domain.ToDo.Companion.NONE_DATE
import src.framework.Identifier
import src.presentation.Styles.Companion.centerAlignedCell
import tornadofx.*
import java.time.LocalDate


class ToDoListView : Fragment() {
//class ToDoListView(token: Identifier) : View() {

    override val scope = super.scope as AppScope

    companion object : KLogging()

    private val token: Identifier by param()

//    private val token = identifier as? Token ?: throw NotImplementedError("The token $token is not a valid token for the application.")

    private val displayArea =
        when (token) {
            is Token.ReminderListView -> DisplayArea.Reminders
            is Token.ToDoListView -> DisplayArea.ToDos
            else -> throw Exception("Invalid token, $token, passed to the the ToDoList view.")
        }

    // Control handles for testing
    // TODO: find a better way of referencing the controls in the tests themselves
    private var table: TableView<ToDo> by singleAssign()
//    var table: TableView<ToDo> by singleAssign()
//    var addButton: Button by singleAssign()
//    var deleteButton: Button by singleAssign()
//    var refreshButton: Button by singleAssign()
//    var editButton: Button by singleAssign()
//    var filterText: TextField by singleAssign()
//    var todayCheckbox: CheckBox by singleAssign()

    val todayOnly = SimpleBooleanProperty(true).apply {
        onChange {
            scope.todoController.refreshRequest.onNext(token)
        }
    }

    private val filter = SimpleStringProperty("").apply {
        onChange { description ->
            if (description.isNullOrBlank())
                scope.todoController.refreshRequest.onNext(token)
            else
                scope.todoController.filterRequest.onNext(
                    token to { todo: ToDo ->
                        description.toLowerCase() in todo.description.toLowerCase()
                    }
//                    token to ToDos.select {
//                        ToDos.description.lowerCase() like "%${description.toLowerCase()}%"
//                    }
                )
        }
        scope.todoController.refreshResponse.subscribe { value = "" }

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
                                    scope.todoController.updateRequest.onNext(updatedToDo)
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

                scope.todoController.addResponse.subscribeBy(
                    onNext = {
                        if (todayOnly.value) {
                            if (it.display) items.add(it)
                        } else {
                            items.add(it)
                        }
                    },
                    onError = scope.alertService::alertError
                )
                fun updateItems(source: Identifier, todos: List<ToDo>) {
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
                scope.todoController.refreshResponse.subscribeBy(
                    onNext = { (source, todos) ->
                        updateItems(source = source, todos = todos)
                    },
                    onError = scope.alertService::alertError
                )
                scope.todoController.filterResponse.subscribeBy(
                    onNext = { (source, todos) ->
                        updateItems(source = source, todos = todos)
                    },
                    onError = scope.alertService::alertError
                )
                scope.todoController.deleteResponse.subscribeBy(
                    onNext = { todo -> items.remove(todo) },
                    onError = scope.alertService::alertError
                )
                scope.todoController.updateResponse.subscribeBy(
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
                    onError = scope.alertService::alertError
                )
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
                                "displayArea" to displayArea
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
                                scope.todoController.deleteRequest.onNext(todo)
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
                    action { scope.todoController.refreshRequest.onNext(token) }
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
                                    "displayArea" to displayArea
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