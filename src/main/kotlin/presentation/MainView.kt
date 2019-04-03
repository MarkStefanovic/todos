package presentation

import app.AppScope
import app.Token
import javafx.scene.text.Font.font
import javafx.scene.text.FontWeight
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
                    add(find<ToDoListView>(scope = scope, params = mapOf("token" to Token.ToDo)))
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
                    add(find<ToDoListView>(scope = scope, params = mapOf("token" to Token.Reminder)))
                }
            }
        }

        scope.todoEventModel.refreshRequest.onNext(Token.ToDo)
    }
}