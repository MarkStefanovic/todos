package src.framework

import org.jetbrains.exposed.sql.Query

abstract class Repository<T : Any> {
    abstract fun add(newItem: T): T?

    abstract fun all(): List<T>?

    abstract fun delete(id: Int): Int?

    abstract fun filter(request: Query): List<T>?

    abstract fun update(item: T): T?
}