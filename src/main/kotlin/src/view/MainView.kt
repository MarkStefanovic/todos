package src.view

import javafx.scene.control.TabPane
import tornadofx.*

class MainView: View("ToDo List"){
    override val root = tabpane {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        add(find(ToDoListView::class, scope))
    }
}