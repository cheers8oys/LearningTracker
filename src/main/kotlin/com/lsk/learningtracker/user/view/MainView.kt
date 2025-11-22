package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.statistics.view.StatisticsView
import com.lsk.learningtracker.studyRecord.view.CalendarMapView
import com.lsk.learningtracker.study.service.StudyRecordService
import com.lsk.learningtracker.studyRecord.service.StatisticsService
import com.lsk.learningtracker.todo.controller.TodoController
import com.lsk.learningtracker.todo.enums.TodoFilter
import com.lsk.learningtracker.todo.filter.TodoFilterManager
import com.lsk.learningtracker.todo.enums.Priority
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.model.TodoSortManager
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.todo.timer.ActiveTimerManager
import com.lsk.learningtracker.todo.timer.TimerException
import com.lsk.learningtracker.todo.view.FilterButtonGroup
import com.lsk.learningtracker.todo.view.TodoDetailModal
import com.lsk.learningtracker.todo.view.TodoListCell
import com.lsk.learningtracker.user.controller.AuthController
import com.lsk.learningtracker.user.model.User
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.time.LocalDate
import java.time.YearMonth

class MainView(
    private val stage: Stage,
    private val user: User,
    private val todoService: TodoService,
    private val studyRecordService: StudyRecordService,
    private val statisticsService: StatisticsService,
    private val onLogout: () -> Unit
) {
    private val todoList = FXCollections.observableArrayList<Todo>()
    private lateinit var todoController: TodoController
    private lateinit var authController: AuthController
    private lateinit var filterButtonGroup: FilterButtonGroup
    private lateinit var calendarView: CalendarMapView
    private lateinit var calendarContainer: VBox

    private val activeTimerManager = ActiveTimerManager()
    private val filterManager = TodoFilterManager()
    private val sortManager = TodoSortManager()

    private var currentMonth = YearMonth.now()
    private var selectedDate = LocalDate.now()

    fun show() {
        initializeControllers()
        initializeCalendarView()

        val root = BorderPane()

        val leftPanel = createLeftPanel()
        val rightPanel = createRightPanel()

        root.left = leftPanel
        root.center = rightPanel

        refreshCalendar()
        refreshTodoList()

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

    private fun initializeCalendarView() {
        calendarView = CalendarMapView { date ->
            handleDateClick(date)
        }
    }

    private fun createLeftPanel(): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER
            prefWidth = 450.0
            style = "-fx-background-color: white;"

            calendarContainer = VBox()

            val monthNav = createMonthNavigation()

            children.addAll(
                Label("📅 학습 캘린더").apply {
                    style = "-fx-font-size: 20px; -fx-font-weight: bold;"
                },
                monthNav,
                calendarContainer
            )
        }
    }

    private fun createMonthNavigation(): HBox {
        val prevButton = Button("◀").apply {
            setOnAction {
                currentMonth = currentMonth.minusMonths(1)
                refreshCalendar()
            }
        }

        val todayButton = Button("오늘").apply {
            setOnAction {
                currentMonth = YearMonth.now()
                selectedDate = LocalDate.now()
                refreshCalendar()
                refreshTodoList()
            }
        }

        val nextButton = Button("▶").apply {
            setOnAction {
                currentMonth = currentMonth.plusMonths(1)
                refreshCalendar()
            }
        }

        return HBox(10.0, prevButton, todayButton, nextButton).apply {
            alignment = Pos.CENTER
        }
    }

    private fun createRightPanel(): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER

            children.addAll(
                createHeader(),
                createInputBox(),
                createFilterBox(),
                createTodoListView(),
                createBottomButtons()
            )
        }
    }

    private fun createHeader(): Label {
        return Label("환영합니다, ${user.username}님!").apply {
            style = "-fx-font-size: 24px; -fx-font-weight: bold;"
        }
    }

    private fun createInputBox(): HBox {
        val todoInputField = TextField().apply {
            promptText = "새 투두 내용을 입력하세요"
            prefWidth = 350.0

            setOnAction {
                handleAddTodo(this)
            }
        }

        val addButton = Button("추가").apply {
            prefWidth = 80.0
            style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
            setOnAction {
                handleAddTodo(todoInputField)
            }
        }

        return HBox(10.0, todoInputField, addButton).apply {
            alignment = Pos.CENTER
        }
    }

    private fun createFilterBox(): HBox {
        filterButtonGroup = FilterButtonGroup { filter ->
            handleFilterChange(filter)
        }
        return filterButtonGroup.createFilterBox()
    }

    private fun createTodoListView(): ListView<Todo> {
        return ListView<Todo>(todoList).apply {
            prefWidth = 600.0
            prefHeight = 400.0
            cellFactory = javafx.util.Callback {
                TodoListCell(
                    onTimerStart = { todo -> handleTimerStart(todo) },
                    onTimerPause = { todo, seconds -> handleTimerPause(todo, seconds) },
                    onTimerReset = { todo -> handleTimerReset(todo) },
                    onComplete = { todo -> handleComplete(todo) },
                    onEdit = { todo -> handleEdit(todo) },
                    onDelete = { todo -> handleDelete(todo) },
                    onCanStartTimer = { todo -> todoController.canStartTimer(todo) },
                    onPriorityChange = { todo, priority -> handlePriorityChange(todo, priority) },
                    getActiveTimerId = { todoController.getActiveTimerId() }
                )
            }
        }
    }

    private fun createLogoutButton(): Button {
        return Button("로그아웃").apply {
            setOnAction {
                authController.logout()
            }
            style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;"
            prefWidth = 100.0
        }
    }

    private fun handleDateClick(date: LocalDate) {
        selectedDate = date

        when {
            date == LocalDate.now() -> refreshTodoList()
            else -> showTodoDetailModal(date)
        }
    }

    private fun showTodoDetailModal(date: LocalDate) {
        val todos = todoService.getTodosByDate(authController.getUserId(), date)
        val studyRecord = studyRecordService.getOrCalculateStudyRecord(authController.getUserId(), date)

        TodoDetailModal().show(stage, date, todos, studyRecord)
    }

    private fun handleAddTodo(inputField: TextField) {
        val content = inputField.text.trim()

        when {
            content.isEmpty() -> return
            else -> {
                todoController.createTodo(content, Priority.MEDIUM)
                inputField.clear()
                refreshTodoList()
                updateStudyRecord()
            }
        }
    }

    private fun handleFilterChange(filter: TodoFilter) {
        todoController.setFilter(filter)
        refreshTodoList()
    }

    private fun handlePriorityChange(todo: Todo, priority: Priority) {
        todoController.updateTodoPriority(todo, priority)
        refreshTodoList()
    }

    private fun handleTimerStart(todo: Todo) {
        try {
            todoController.startTimer(todo)
            refreshTodoList()
        } catch (e: TimerException.AlreadyRunningException) {
            showError("타이머 실행 불가", e.message ?: "다른 타이머가 실행 중입니다.")
        }
    }

    private fun handleTimerPause(todo: Todo, elapsedSeconds: Int) {
        try {
            todoController.pauseTimer(todo, elapsedSeconds)
            updateStudyRecord()
            refreshTodoList()
        } catch (e: TimerException.SaveFailedException) {
            showError("저장 실패", "타이머 저장에 실패했습니다.")
        }
    }

    private fun handleTimerReset(todo: Todo) {
        try {
            todoController.resetTimer(todo)
            updateStudyRecord()
        } catch (e: TimerException.SaveFailedException) {
            showError("초기화 실패", "타이머 초기화에 실패했습니다.")
        }
    }

    private fun handleComplete(todo: Todo) {
        todoController.completeTodo(todo)
        refreshTodoList()
        updateStudyRecord()
        refreshCalendar()
    }

    private fun handleEdit(todo: Todo) {
        val newContent = showEditDialog(todo.content)
        when {
            newContent == null -> return
            newContent.trim().isEmpty() -> return
            newContent.trim() == todo.content -> return
            else -> {
                todoController.updateTodoContent(todo, newContent)
                refreshTodoList()
            }
        }
    }

    private fun handleDelete(todo: Todo) {
        todoController.deleteTodo(todo)
        refreshTodoList()
        updateStudyRecord()
        refreshCalendar()
    }

    private fun showEditDialog(currentContent: String): String? {
        val dialog = TextInputDialog(currentContent)
        dialog.title = "투두 수정"
        dialog.headerText = "투두 내용을 수정하세요"
        dialog.contentText = "내용:"

        val result = dialog.showAndWait()
        return result.orElse(null)
    }

    private fun showError(title: String, message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    private fun refreshTodoList() {
        val todos = todoController.getTodayTodos()
        todoList.setAll(todos)
    }

    private fun refreshCalendar() {
        val startDate = currentMonth.atDay(1)
        val endDate = currentMonth.atEndOfMonth()

        val recordMap = studyRecordService.getOrCalculateStudyRecords(
            authController.getUserId(),
            startDate,
            endDate
        )

        val calendar = calendarView.createCalendar(currentMonth, recordMap)

        calendarContainer.children.clear()
        calendarContainer.children.add(calendar)
    }

    private fun updateStudyRecord() {
        studyRecordService.updateStudyRecord(authController.getUserId(), LocalDate.now())
        refreshCalendar()
    }

    private fun createBottomButtons(): HBox {
        val statisticsButton = Button("📊 통계").apply {
            setOnAction {
                showStatistics()
            }
            style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
            prefWidth = 100.0
        }

        val logoutButton = Button("로그아웃").apply {
            setOnAction {
                authController.logout()
            }
            style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;"
            prefWidth = 100.0
        }

        return HBox(10.0, statisticsButton, logoutButton).apply {
            alignment = Pos.CENTER
        }
    }

    private fun showStatistics() {
        val statisticsView = StatisticsView(authController.getUserId(), statisticsService)
        statisticsView.show(stage)
    }

}
