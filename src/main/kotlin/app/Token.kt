package app

import framework.Identifier


sealed class Token {
    object ToDo : Token(), Identifier
    object Reminder : Token(), Identifier
}
