package src.app

import src.framework.Identifier


sealed class Token {
    object ReminderEditor : Token(), Identifier {
        override val id = "Reminders"
    }

    object ReminderListView : Token(), Identifier {
        override val id = "Add/Edit Reminders"
    }

    object ToDoEditor : Token(), Identifier {
        override val id = "Add/Edit ToDos"
    }

    object ToDoListView : Token(), Identifier {
        override val id = "ToDos"
    }
}
