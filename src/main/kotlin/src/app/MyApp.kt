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
import src.view.TornadoAlertService
import src.view.TornadoConfirmationService
import tornadofx.*

val logger = KotlinLogging.logger { }


class MyApp : App(MainView::class, Styles::class) {
    private val alertService = TornadoAlertService()
    private val confirmationService = TornadoConfirmationService()
    private val db = Db(url = "jdbc:sqlite:./app.db", driver = "org.sqlite.JDBC")
    private val scheduler = SchedulerProvider()
    private val toDoController = ToDoController(
        db = db,
        schedulerProvider = scheduler,
        alertService = alertService,
        confirmationService = confirmationService
    )
    private val reminderController = ToDoController(
        db = db,
        schedulerProvider = scheduler,
        alertService = alertService,
        confirmationService = confirmationService
    )

    init {
        logger.debug("Starting App")

        scope = AppScope(
            toDoController = toDoController,
            reminderController = reminderController,
            alertService = alertService
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
                        it[displayArea] = todo.displayArea.name
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