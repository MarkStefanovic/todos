package dummies

import src.framework.Repository

class DummyRepository<T : Any>(var items: MutableList<T>) : Repository<T>() {
    override fun add(newItem: T): T? {
        items.add(newItem)
        return newItem
    }

    override fun all(): List<T>? = items

    override fun delete(item: T): T? {
        items.remove(item)
        return item
    }

    override fun filter(criteria: (T) -> Boolean): List<T>? = items.filter(criteria)

    override fun update(item: T): T? {
        items.remove(item)
        items.add(item)
        return item
    }
}