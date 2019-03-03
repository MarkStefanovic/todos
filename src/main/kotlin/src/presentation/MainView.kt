package src.presentation

import javafx.scene.text.Font.font
import javafx.scene.text.FontWeight
import src.app.AppScope
import src.framework.Token
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
                            token = Token.ToDoListView,
                            alertService = scope.alertService,
                            eventModel = scope.toDoEventModel,
                            confirmationService = scope.confirmationService
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
                            token = Token.ReminderListView,
                            alertService = scope.alertService,
                            eventModel = scope.toDoEventModel,
                            confirmationService = scope.confirmationService
                        )
                    )
                }
            }
        }

        scope.toDoEventModel.refreshRequest.onNext(Token.ToDoListView)
    }
}