package presentation

import app.AppScope
import domain.DisplayArea
import javafx.scene.text.Font.font
import javafx.scene.text.FontWeight
import tornadofx.*

class MainView: View("ToDo List") {
    override val scope = super.scope as AppScope

    private val todos = ToDoListView(
        alertService = scope.alertService,
        confirmationService = scope.confirmationService,
        displayArea = DisplayArea.ToDos,
        eventModel = scope.todoEventModel
    )

    private val reminders = ToDoListView(
        alertService = scope.alertService,
        confirmationService = scope.confirmationService,
        displayArea = DisplayArea.Reminders,
        eventModel = scope.reminderEventModel
    )

    override val root = borderpane {
        val labelHeaderFont = font("Arial", FontWeight.BOLD, 14.0)
        center {
            borderpane {
                top {
                    label("ToDos") {
                        paddingAll = 4.0
                        font = labelHeaderFont
                    }
                }
                center {
                    add(todos)
                }
            }
        }
        right {
            borderpane {
                top {
                    label("Reminders") {
                        paddingAll = 4.0
                        font = labelHeaderFont
                    }
                }
                center {
                    add(reminders)
                }
            }
        }

        scope.todoEventModel.refreshRequest.onNext(Unit)
        scope.reminderEventModel.refreshRequest.onNext(Unit)
    }

    override fun onDock() {
        todos.root.requestFocus()
    }
}