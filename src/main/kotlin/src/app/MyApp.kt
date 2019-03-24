package src.app

import javafx.scene.image.Image
import javafx.stage.Stage
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insert
import src.domain.ToDoRepository
import src.domain.ToDos
import src.domain.birthdays
import src.domain.holidays
import src.framework.RepositoryController
import src.presentation.MainView
import src.presentation.Styles
import src.services.AsyncSchedulerProvider
import src.services.PopupAlertService
import src.services.PopupConfirmationService
import src.services.SqlDb
import tornadofx.*

val logger = KotlinLogging.logger { }

class MyApp : App(MainView::class, Styles::class) {
    private val alertService = PopupAlertService()
    private val confirmationService = PopupConfirmationService()
    private val db = SqlDb(url = "jdbc:sqlite:./app.db", driver = "org.sqlite.JDBC")
    private val schedulerProvider = AsyncSchedulerProvider()
    private val toDoRepository = ToDoRepository(db = db)
    private val todoController = RepositoryController(
        schedulerProvider = schedulerProvider,
        alertService = alertService,
        repository = toDoRepository
    )

    init {
        logger.debug("Starting App")

        scope = AppScope(
            alertService = alertService,
            confirmationService = confirmationService,
            todoController = todoController
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