package src.app

import src.framework.Identifier


//object ReminderEditor: SignalSourceIdentifier {
//    override val id = "Reminder Editor"
//}
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

//sealed class Token {
//    object ReminderEditor : Token()
//    object ReminderListView : Token()
//    object ToDoEditor : Token()
//    object ToDoListView : Token()
//
//
//}
//enum class Token {
//    ReminderEditor,
//    ReminderListView,
//    ToDoEditor,
//    ToDoListView,
//}