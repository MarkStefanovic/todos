package presentation

import app.AppScope
import app.Token
import javafx.scene.text.Font.font
import javafx.scene.text.FontWeight
import tornadofx.*

class MainView: View("ToDo List") {
    override val scope = super.scope as AppScope

    private val todos = find<ToDoListView>(scope = scope, params = mapOf("token" to Token.ToDo))
    private val reminders = find<ToDoListView>(scope = scope, params = mapOf("token" to Token.Reminder))
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

        scope.todoEventModel.refreshRequest.onNext(Token.ToDo)
    }

    override fun onDock() {
        todos.root.requestFocus()
    }
}