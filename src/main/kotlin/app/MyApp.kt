package app

import domain.*
import framework.RepositoryEventModel
import javafx.scene.image.Image
import javafx.stage.Stage
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import presentation.MainView
import presentation.Styles
import services.AsyncSchedulerProvider
import services.PopupAlertService
import services.PopupConfirmationService
import services.SqlDatabaseService
import tornadofx.*

val logger = KotlinLogging.logger { }

class MyApp : App(MainView::class, Styles::class) {
    private val alertService = PopupAlertService()
    private val confirmationService = PopupConfirmationService()
    private val db = SqlDatabaseService(url = "jdbc:sqlite:./app.db", driver = "org.sqlite.JDBC")
    private val schedulerProvider = AsyncSchedulerProvider()

    private val toDoRepository = ToDoRepository(db = db, displayArea = DisplayArea.ToDos)
    private val toDoEventModel = RepositoryEventModel<ToDo>(schedulerProvider = schedulerProvider)
    private val todoController = ToDoController(
        schedulerProvider = schedulerProvider,
        alertService = alertService,
        eventModel = toDoEventModel,
        repository = toDoRepository
    )

    private val reminderRepository = ToDoRepository(db = db, displayArea = DisplayArea.Reminders)
    private val reminderEventModel = RepositoryEventModel<ToDo>(schedulerProvider = schedulerProvider)
    private val reminderController = ToDoController(
        schedulerProvider = schedulerProvider,
        alertService = alertService,
        eventModel = reminderEventModel,
        repository = reminderRepository
    )

    init {
        logger.debug("Starting App")

        scope = AppScope(
            alertService = alertService,
            confirmationService = confirmationService,
            todoEventModel = toDoEventModel,
            reminderEventModel = reminderEventModel
        )

        // insert initial sql rows if creating new db
        db.execute {
            SchemaUtils.create(ToDos)
            val initialRecords = holidays.union(birthdays)
            initialRecords.forEach { todo ->
                if (ToDos.select { ToDos.description eq todo.description }.count() == 0) {
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

        todoController.start()
        reminderController.start()
    }

    override fun start(stage: Stage) {
        val img = Image("file:app.png")
        addStageIcon(img)
        super.start(stage)
    }
}