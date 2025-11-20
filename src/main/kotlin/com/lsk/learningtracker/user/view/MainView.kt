package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.todo.controller.TodoController
import com.lsk.learningtracker.todo.filter.TodoFilter
import com.lsk.learningtracker.todo.filter.TodoFilterManager
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.todo.timer.ActiveTimerManager
import com.lsk.learningtracker.todo.timer.TimerException
import com.lsk.learningtracker.todo.view.FilterButtonGroup
import com.lsk.learningtracker.todo.view.TodoListCell
import com.lsk.learningtracker.user.controller.AuthController
import com.lsk.learningtracker.user.model.User
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

class MainView(
    private val stage: Stage,
    private val user: User,
    private val todoService: TodoService,
    private val onLogout: () -> Unit
) {
    private val todoList = FXCollections.observableArrayList<Todo>()
    private lateinit var todoController: TodoController
    private lateinit var authController: AuthController
    private lateinit var filterButtonGroup: FilterButtonGroup

    private val activeTimerManager = ActiveTimerManager()
    private val filterManager = TodoFilterManager()

    fun show() {
        initializeControllers()

        val root = createRootLayout()
        val inputBox = createTodoInputBox()
        val filterBox = createFilterBox()
        val listView = createTodoListView()
        val logoutButton = createLogoutButton()

        root.children.addAll(inputBox, filterBox, listView, logoutButton)

        refreshTodoList()

        val scene = Scene(root, 650.0, 650.0)
        stage.scene = scene
        stage.title = "학습 Tracker - 투두리스트"
        stage.show()
    }

    private fun initializeControllers() {
        authController = AuthController(user, onLogout)
        todoController = TodoController(
            userId = authController.getUserId(),
            todoService = todoService,
            activeTimerManager = activeTimerManager,
            filterManager = filterManager
        )
    }

    private fun createRootLayout(): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER

            children.add(
                Label("환영합니다, ${user.username}님!").apply {
                    style = "-fx-font-size: 24px; -fx-font-weight: bold;"
                }
            )
        }
    }

    private fun createTodoInputBox(): HBox {
        val todoInputField = TextField().apply {
            promptText = "새 투두 내용을 입력하세요"
            prefWidth = 300.0
        }

        val addButton = Button("추가").apply {
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
                    onCanStartTimer = { todo -> todoController.canStartTimer(todo) }
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

    private fun handleAddTodo(inputField: TextField) {
        val content = inputField.text.trim()
        when {
            content.isEmpty() -> return
            else -> {
                todoController.createTodo(content)
                inputField.clear()
                refreshTodoList()
            }
        }
    }

    private fun handleFilterChange(filter: TodoFilter) {
        todoController.setFilter(filter)
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
        } catch (e: TimerException.SaveFailedException) {
            showError("저장 실패", "타이머 저장에 실패했습니다.")
        }
    }

    private fun handleTimerReset(todo: Todo) {
        try {
            todoController.resetTimer(todo)
        } catch (e: TimerException.SaveFailedException) {
            showError("초기화 실패", "타이머 초기화에 실패했습니다.")
        }
    }

    private fun handleComplete(todo: Todo) {
        todoController.completeTodo(todo)
        refreshTodoList()
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
}
