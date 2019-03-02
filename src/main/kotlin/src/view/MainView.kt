package src.view

import javafx.scene.text.Font.font
import javafx.scene.text.FontWeight
import src.app.AppScope
import src.controller.Token
import tornadofx.*

class MainView: View("ToDo List") {
    override val scope = super.scope as AppScope
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
                    add(
                        ToDoListView(
                            controller = scope.toDoController,
                            token = Token.TODO_LIST_VIEW,
                            alertService = scope.alertService
                        )
                    )
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
                    add(
                        ToDoListView(
                            controller = scope.reminderController,
                            token = Token.REMINDER_LIST_VIEW,
                            alertService = scope.alertService
                        )
                    )
                }
            }
        }
    }

    init {
        scope.toDoController.refreshRequest.onNext(Token.TODO_LIST_VIEW)
        scope.reminderController.refreshRequest.onNext(Token.REMINDER_LIST_VIEW)
    }
}