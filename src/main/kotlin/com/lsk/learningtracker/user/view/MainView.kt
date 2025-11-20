package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.todo.controller.TodoController
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.service.TodoService
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

    fun show() {
        initializeControllers()

        val root = createRootLayout()
        val inputBox = createTodoInputBox()
        val listView = createTodoListView()
        val logoutButton = createLogoutButton()

        root.children.addAll(inputBox, listView, logoutButton)

        refreshTodoList()

        val scene = Scene(root, 650.0, 600.0)
        stage.scene = scene
        stage.title = "학습 Tracker - 투두리스트"
        stage.show()
    }

    private fun initializeControllers() {
        authController = AuthController(user, onLogout)
        todoController = TodoController(authController.getUserId(), todoService)
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
                    onDelete = { todo -> handleDelete(todo) }
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
                val newTodo = todoController.createTodo(content)
                todoList.add(newTodo)
                inputField.clear()
            }
        }
    }

    private fun handleTimerStart(todo: Todo) {
        todoController.startTimer(todo)
        refreshTodoList()
    }

    private fun handleTimerPause(todo: Todo, elapsedSeconds: Int) {
        todoController.pauseTimer(todo, elapsedSeconds)
        refreshTodoList()
    }

    private fun handleTimerReset(todo: Todo) {
        todoController.resetTimer(todo)
        refreshTodoList()
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

    private fun refreshTodoList() {
        val todos = todoController.getTodayTodos()
        todoList.setAll(todos)
    }
}
