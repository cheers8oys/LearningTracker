package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.statistics.view.StatisticsView
import com.lsk.learningtracker.studyRecord.view.CalendarView
import com.lsk.learningtracker.todo.view.TodoView
import com.lsk.learningtracker.study.service.StudyRecordService
import com.lsk.learningtracker.studyRecord.service.StatisticsService
import com.lsk.learningtracker.todo.controller.TodoController
import com.lsk.learningtracker.todo.filter.TodoFilterManager
import com.lsk.learningtracker.todo.model.TodoSortManager
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.todo.timer.ActiveTimerManager
import com.lsk.learningtracker.todo.view.TodoDetailModal
import com.lsk.learningtracker.user.controller.AuthController
import com.lsk.learningtracker.user.model.User
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.time.LocalDate


class MainView(
    private val stage: Stage,
    private val user: User,
    private val todoService: TodoService,
    private val studyRecordService: StudyRecordService,
    private val statisticsService: StatisticsService,
    private val onLogout: () -> Unit
) {
    private lateinit var todoController: TodoController
    private lateinit var authController: AuthController
    private lateinit var calendarView: CalendarView
    private lateinit var todoView: TodoView

    private val activeTimerManager = ActiveTimerManager()
    private val filterManager = TodoFilterManager()
    private val sortManager = TodoSortManager()


    fun show() {
        initializeControllers()
        initializePanels()

        val root = createLayout()

        calendarView.refresh()
        todoView.refresh()

        val scene = Scene(root, 1100.0, 700.0)
        stage.scene = scene
        stage.title = "학습 Tracker"
        stage.show()
    }

    private fun initializeControllers() {
        authController = AuthController(user, onLogout)
        todoController = TodoController(
            userId = authController.getUserId(),
            todoService = todoService,
            activeTimerManager = activeTimerManager,
            filterManager = filterManager,
            sortManager = sortManager
        )
    }

    private fun initializePanels() {
        calendarView = CalendarView(
            userId = authController.getUserId(),
            studyRecordService = studyRecordService,
            onDateClick = { date -> handleDateClick(date) }
        )

        todoView = TodoView(
            stage = stage,
            username = user.username,
            todoController = todoController,
            onStudyRecordUpdate = { updateStudyRecord() }
        )
    }

    private fun createLayout(): BorderPane {
        return BorderPane().apply {
            left = calendarView.create()
            center = createRightPanel()
        }
    }

    private fun createRightPanel(): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER

            children.addAll(
                todoView.create(),
                createBottomButtons()
            )
        }
    }

    private fun createBottomButtons(): HBox {
        val statisticsButton = Button("📊 통계").apply {
            setOnAction { showStatistics() }
            style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
            prefWidth = 100.0
        }

        val logoutButton = Button("로그아웃").apply {
            setOnAction { authController.logout() }
            style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;"
            prefWidth = 100.0
        }

        return HBox(10.0, statisticsButton, logoutButton).apply {
            alignment = Pos.CENTER
        }
    }

    private fun handleDateClick(date: LocalDate) {
        when {
            date == LocalDate.now() -> todoView.refresh()
            else -> showTodoDetailModal(date)
        }
    }

    private fun showTodoDetailModal(date: LocalDate) {
        val todos = todoService.getTodosByDate(authController.getUserId(), date)
        val studyRecord = studyRecordService.getOrCalculateStudyRecord(
            authController.getUserId(),
            date
        )
        TodoDetailModal().show(stage, date, todos, studyRecord)
    }

    private fun updateStudyRecord() {
        studyRecordService.updateStudyRecord(authController.getUserId(), LocalDate.now())
        calendarView.refresh()
    }

    private fun showStatistics() {
        val statisticsView = StatisticsView(authController.getUserId(), statisticsService)
        statisticsView.show(stage)
    }
}
