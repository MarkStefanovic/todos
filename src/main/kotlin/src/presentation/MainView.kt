package src.presentation

import javafx.scene.text.Font.font
import javafx.scene.text.FontWeight
import src.app.AppScope
import src.app.Token
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
                    add(find<ToDoListView>(scope = scope, params = mapOf("token" to Token.ToDoListView)))
//                    add(ToDoListView(token = Token.ToDoListView))
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
                    add(find<ToDoListView>(scope = scope, params = mapOf("token" to Token.ReminderListView)))
//                    add(ToDoListView(token = Token.ReminderListView))
                }
            }
        }

        scope.todoController.refreshRequest.onNext(Token.ToDoListView)
    }
}