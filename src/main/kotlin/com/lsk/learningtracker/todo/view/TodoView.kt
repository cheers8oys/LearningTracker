package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.todo.controller.TodoController
import com.lsk.learningtracker.todo.enums.Priority
import com.lsk.learningtracker.todo.enums.TodoFilter
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.timer.TimerException
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

class TodoView(
    private val stage: Stage,
    private val username: String,
    private val todoController: TodoController,
    private val onStudyRecordUpdate: () -> Unit
) {
    private val todoList: ObservableList<Todo> = FXCollections.observableArrayList()
    private lateinit var filterButtonGroup: FilterButtonGroup
    private lateinit var todoInputField: TextField

    fun create(): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER

            children.addAll(
                createHeader(),
                createInputBox(),
                createFilterBox(),
                createTodoListView()
            )
        }
    }

    fun refresh() {
        refreshTodoList()
    }

    private fun createHeader(): Label {
        return Label("환영합니다, ${username}님!").apply {
            style = "-fx-font-size: 24px; -fx-font-weight: bold;"
        }
    }

    private fun createInputBox(): HBox {
        todoInputField = TextField().apply {
            promptText = "새 투두 내용을 입력하세요"
            prefWidth = 350.0
            setOnAction {
                handleAddTodo()
            }
        }

        val addButton = Button("추가").apply {
            prefWidth = 80.0
            style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
            setOnAction {
                handleAddTodo()
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
        return ListView(todoList).apply {
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

    private fun handleAddTodo() {
        val content = todoInputField.text.trim()
        when {
            content.isEmpty() -> return
            else -> {
                todoController.createTodo(content, Priority.MEDIUM)
                todoInputField.clear()
                refreshTodoList()
                onStudyRecordUpdate()
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
            onStudyRecordUpdate()
            refreshTodoList()
        } catch (e: TimerException.SaveFailedException) {
            showError("저장 실패", "타이머 저장에 실패했습니다.")
        }
    }

    private fun handleTimerReset(todo: Todo) {
        try {
            todoController.resetTimer(todo)
            onStudyRecordUpdate()
        } catch (e: TimerException.SaveFailedException) {
            showError("초기화 실패", "타이머 초기화에 실패했습니다.")
        }
    }

    private fun handleComplete(todo: Todo) {
        todoController.completeTodo(todo)
        refreshTodoList()
        onStudyRecordUpdate()
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
        onStudyRecordUpdate()
    }

    private fun refreshTodoList() {
        val todos = todoController.getTodayTodos()
        todoList.setAll(todos)
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
}