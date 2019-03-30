package framework

abstract class Repository<T : Any> {
    abstract fun add(newItem: T): T?

    abstract fun all(): List<T>?

    abstract fun delete(item: T): T?

    abstract fun filter(criteria: (T) -> Boolean): List<T>?

    abstract fun update(item: T): T?
}