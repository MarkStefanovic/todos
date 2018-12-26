package src.app

import javafx.scene.image.Image
import javafx.stage.Stage
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insert
import src.controller.SchedulerProvider
import src.controller.ToDoController
import src.model.ToDos
import src.model.birthdays
import src.model.holidays
import src.view.MainView
import tornadofx.*

val logger = KotlinLogging.logger { }


class MyApp: App(MainView::class, Styles::class) {
    val db = Db(url = "jdbc:sqlite:./app.db", driver = "org.sqlite.JDBC")

    init {
        logger.debug("Starting App")

        scope = AppScope(
            toDoController = ToDoController(
                db = db,
                schedulerProvider = SchedulerProvider()
            )
        )

        // insert initial sql rows if creating new db
        db.execute {
            if (!ToDos.exists()) {
                SchemaUtils.create(ToDos)
                val initialRecords = holidays.union(birthdays)
                initialRecords.forEach { todo ->
                    ToDos.insert {
                        it[description] = todo.description
                        it[frequency] = todo.frequency
                        it[weekday] = todo.weekday
                        it[monthday] = todo.monthday
                        it[month] = todo.month
                        it[year] = todo.year
                        it[weekNumber] = todo.weekNumber
                        it[expireDays] = todo.expireDays
                        it[advanceNotice] = todo.advanceNotice
                    }
                }
            }
        }
    }

    override fun start(stage: Stage) {
        val img = Image("file:app.png")
        addStageIcon(img)
        super.start(stage)
    }
}