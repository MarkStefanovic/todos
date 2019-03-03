package src.app

import javafx.scene.image.Image
import javafx.stage.Stage
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insert
import src.domain.*
import src.framework.EventBus
import src.framework.EventModel
import src.presentation.MainView
import src.presentation.Styles
import src.services.AsyncSchedulerProvider
import src.services.Db
import src.services.PopupAlertService
import src.services.PopupConfirmationService
import tornadofx.*

val logger = KotlinLogging.logger { }


class MyApp : App(MainView::class, Styles::class) {
    private val alertService = PopupAlertService()
    private val confirmationService = PopupConfirmationService()
    private val db = Db(url = "jdbc:sqlite:./app.db", driver = "org.sqlite.JDBC")
    private val schedulerProvider = AsyncSchedulerProvider()
    private val toDoEventModel = EventModel<ToDo>(schedulerProvider)
    private val toDoRepository = ToDoRepository(db = db)

    init {
        logger.debug("Starting App")

        scope = AppScope(
            alertService = alertService,
            confirmationService = confirmationService,
            toDoEventModel = toDoEventModel
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

        // start up the event bus
        EventBus(
            schedulerProvider = schedulerProvider,
            alertService = alertService,
            repository = toDoRepository,
            eventModel = toDoEventModel
        )
    }

    override fun start(stage: Stage) {
        val img = Image("file:app.png")
        addStageIcon(img)
        super.start(stage)
    }
}