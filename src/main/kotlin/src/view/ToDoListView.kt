package src.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.reactivex.rxkotlin.subscribeBy
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.*
import javafx.stage.Modality
import mu.KLogging
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import src.app.AppScope
import src.app.Styles.Companion.centerAlignedCell
import src.app.alertError
import src.model.ToDo
import src.model.ToDos
import tornadofx.*


class ToDoListView : View("ToDos") {
    companion object: KLogging()

    override val scope = super.scope as AppScope

    // Control handles for testing
    var table: TableView<ToDo> by singleAssign()
    var addButton: Button by singleAssign()
    var deleteButton: Button by singleAssign()
    var refreshButton: Button by singleAssign()
    var editButton: Button by singleAssign()
    var filterText: TextField by singleAssign()
    var todayCheckbox: CheckBox by singleAssign()
    var deleteConfirmation: Alert? = null

    val todayOnly = SimpleBooleanProperty(true).apply {
        onChange {
            scope.toDoController.refreshRequest.onNext("ToDoListView")
        }
    }

    private val filter = SimpleStringProperty().apply {
        onChange { description ->
            if (description.isNullOrBlank())
                scope.toDoController.refreshRequest.onNext("ToDoListView")
            else
                scope.toDoController.filterRequest.onNext(
                    "ToDoListView" to ToDos.select {
                        ToDos.description.lowerCase() like "%${ description.toLowerCase() }%"
                    }
                )
        }
        scope.toDoController.refreshResponse.subscribe { value = "" }
    }

    override val root = borderpane {
        paddingAll = 4

        center {
            table = tableview<ToDo> {
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
                (column("Complete", ToDo::complete) as TableColumn<ToDo, Boolean?>).useCheckbox(editable = true)

                onEditCommit {
                    scope.toDoController.updateRequest.onNext(it)
                }
                scope.toDoController.addResponse.subscribeBy(
                    onNext = {
                        if (todayOnly.value) {
                            if (it.display) items.add(it)
                        } else {
                            items.add(it)
                        }
                    },
                    onError = ::alertError
                )
                scope.toDoController.refreshResponse.subscribeBy(
                    onNext = { (token, todos) ->
                        if (token == "ToDoListView") {
                            if (todayOnly.value) {
                                items.setAll(todos.filter { todo -> todo.display })
                            } else {
                                items.setAll(todos)
                            }
                        }
                    },
                    onError = ::alertError
                )
                scope.toDoController.filterResponse.subscribeBy(
                    onNext = { (token, todos) ->
                        if (token == "ToDoListView") {
                            if (todayOnly.value) {
                                items.setAll(todos.filter { todo -> todo.display })
                            } else {
                                items.setAll(todos)
                            }
                        }
                    },
                    onError = ::alertError
                )
                scope.toDoController.deleteResponse.subscribeBy(
                    onNext = { id -> items.removeIf { it.id == id } },
                    onError = ::alertError
                )
                scope.toDoController.updateResponse.subscribeBy(
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
                    onError = ::alertError
                )
                scope.toDoController.refreshRequest.onNext("ToDoListView")
            }
        }

        top {
            toolbar {
                addButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Add ToDo (CTRL + A)")
                    shortcut("Ctrl+A")
                    action {
                        ToDoEditor(scope = scope, mode = "App", todo = ToDo.default()).openModal(
                            primaryStage.style, Modality.WINDOW_MODAL)
                    }
                }
                deleteButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH_ALT).apply {
                        glyphSize = 14.0
                    }
                    tooltip("Delete selected ToDo (CTRL + X)")
                    shortcut("Ctrl+X")
                    action {
                        table.selectionModel.selectedItem?.let { todo ->
                            scope.toDoController.deleteRequest.onNext(todo.id)
                        }
                    }
                    enableWhen(table.selectionModel.selectedItemProperty().isNotNull)
                }
                refreshButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.REFRESH).apply { glyphSize = 14.0 }
                    tooltip("Refresh ToDos (CTRL + R)")
                    shortcut("Ctrl+R")
                    action { scope.toDoController.refreshRequest.onNext("ToDoListView") }
                }
                editButton = button {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT).apply { glyphSize = 14.0 }
                    tooltip("Edit ToDo (CTRL + E")
                    shortcut("CTRL + E")
                    action {
                        table.selectionModel.selectedItem?.let {
                            ToDoEditor(scope = scope, mode = "Edit", todo = it).openModal(
                                primaryStage.style, Modality.WINDOW_MODAL)
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