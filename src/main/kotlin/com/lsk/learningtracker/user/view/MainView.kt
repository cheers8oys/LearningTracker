package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.model.TodoStatus
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.user.model.User
import javafx.animation.AnimationTimer
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

    fun show() {
        val root = VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER

            children.addAll(
                Label("환영합니다, ${user.username}님!").apply {
                    style = "-fx-font-size: 24px; -fx-font-weight: bold;"
                }
            )
        }

        val todoInputField = TextField().apply {
            promptText = "새 투두 내용을 입력하세요"
            prefWidth = 300.0
        }

        val addButton = Button("추가").apply {
            setOnAction {
                val content = todoInputField.text.trim()
                if (content.isNotEmpty()) {
                    val newTodo = todoService.createTodo(userId = user.username.hashCode().toLong(), content = content)
                    todoList.add(newTodo)
                    todoInputField.clear()
                }
            }
        }

        val inputBox = HBox(10.0, todoInputField, addButton).apply {
            alignment = Pos.CENTER
        }

        val listView = ListView<Todo>(todoList).apply {
            prefWidth = 600.0
            prefHeight = 400.0
            cellFactory = javafx.util.Callback {
                object : ListCell<Todo>() {
                    private val timerLabel = Label()
                    private val startPauseButton = Button("시작")
                    private val resetButton = Button("초기화")
                    private val completeButton = Button("완료").apply {
                        style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
                    }
                    private val editButton = Button("수정")
                    private val deleteButton = Button("삭제").apply {
                        style = "-fx-background-color: #f44336; -fx-text-fill: white;"
                    }
                    private val hbox = HBox(10.0)
                    private val label = Label()

                    private var timerRunning = false
                    private var startTimeNs: Long = 0L
                    private var animationTimer: AnimationTimer? = null
                    private var elapsedSeconds = 0

                    init {
                        hbox.alignment = Pos.CENTER_LEFT
                        hbox.children.addAll(label, timerLabel, startPauseButton, resetButton, completeButton, editButton, deleteButton)

                        startPauseButton.setOnAction {
                            if (!timerRunning) {
                                startTimer()
                            } else {
                                pauseTimer()
                            }
                        }
                        resetButton.setOnAction {
                            resetTimer()
                        }
                    }

                    private fun startTimer() {
                        val currentItem = item ?: return

                        if (currentItem.status == TodoStatus.PENDING) {
                            currentItem.status = TodoStatus.IN_PROGRESS
                            todoService.updateTodo(currentItem)
                            refreshTodoList()
                        }

                        timerRunning = true
                        startPauseButton.text = "일시정지"
                        startTimeNs = System.nanoTime()

                        animationTimer = object : AnimationTimer() {
                            override fun handle(now: Long) {
                                val delta = (now - startTimeNs) / 1_000_000_000
                                timerLabel.text = formatSeconds(elapsedSeconds + delta.toInt())
                            }
                        }.also { it.start() }
                    }

                    private fun pauseTimer() {
                        timerRunning = false
                        startPauseButton.text = "시작"
                        animationTimer?.stop()
                        animationTimer = null

                        val delta = ((System.nanoTime() - startTimeNs) / 1_000_000_000).toInt()
                        elapsedSeconds += delta

                        timerLabel.text = formatSeconds(elapsedSeconds)
                        val currentItem = item ?: return
                        todoService.updateTimerSeconds(user.username.hashCode().toLong(), currentItem.id, elapsedSeconds)
                        refreshTodoList()
                    }

                    private fun resetTimer() {
                        animationTimer?.stop()
                        animationTimer = null
                        timerRunning = false
                        startPauseButton.text = "시작"
                        elapsedSeconds = 0
                        timerLabel.text = formatSeconds(0)

                        val currentItem = item ?: return
                        todoService.updateTimerSeconds(user.username.hashCode().toLong(), currentItem.id, 0)
                        refreshTodoList()
                    }

                    private fun formatSeconds(seconds: Int): String {
                        val min = seconds / 60
                        val sec = seconds % 60
                        return String.format("%02d:%02d", min, sec)
                    }

                    override fun updateItem(item: Todo?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty || item == null) {
                            text = null
                            graphic = null
                            animationTimer?.stop()
                            animationTimer = null
                        } else {
                            label.text = "[${item.status}] ${item.content}"
                            elapsedSeconds = item.timerSeconds
                            timerLabel.text = formatSeconds(elapsedSeconds)
                            graphic = hbox

                            val isCompleted = item.status == TodoStatus.COMPLETED
                            completeButton.isDisable = isCompleted
                            startPauseButton.isDisable = isCompleted
                            resetButton.isDisable = isCompleted
                            editButton.isDisable = isCompleted

                            if (isCompleted && timerRunning) {
                                animationTimer?.stop()
                                animationTimer = null
                                timerRunning = false
                                startPauseButton.text = "시작"
                            }

                            completeButton.setOnAction {
                                if (timerRunning) {
                                    animationTimer?.stop()
                                    animationTimer = null
                                    timerRunning = false
                                    startPauseButton.text = "시작"

                                    val delta = ((System.nanoTime() - startTimeNs) / 1_000_000_000).toInt()
                                    elapsedSeconds += delta
                                    todoService.updateTimerSeconds(user.username.hashCode().toLong(), item.id, elapsedSeconds)
                                }

                                item.status = TodoStatus.COMPLETED
                                item.completedAt = java.time.LocalDateTime.now()
                                todoService.updateTodo(item)
                                refreshTodoList()
                            }

                            editButton.setOnAction {
                                val dialog = TextInputDialog(item.content)
                                dialog.title = "투두 수정"
                                dialog.headerText = "투두 내용을 수정하세요"
                                dialog.contentText = "내용:"

                                val result = dialog.showAndWait()
                                result.ifPresent { newContent ->
                                    if (newContent.trim().isNotEmpty() && newContent.trim() != item.content) {
                                        item.content = newContent.trim()
                                        todoService.updateTodo(item)
                                        refreshTodoList()
                                    }
                                }
                            }

                            deleteButton.setOnAction {
                                todoService.deleteTodo(item.id)
                                refreshTodoList()
                            }
                        }
                    }
                }
            }
        }

        val logoutButton = Button("로그아웃").apply {
            setOnAction {
                onLogout()
            }
            style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;"
            prefWidth = 100.0
        }

        root.children.addAll(inputBox, listView, logoutButton)

        refreshTodoList()

        val scene = Scene(root, 650.0, 600.0)
        stage.scene = scene
        stage.title = "학습 Tracker - 투두리스트"
        stage.show()
    }

    private fun refreshTodoList() {
        val todos = todoService.getTodayTodos(user.username.hashCode().toLong())
        todoList.setAll(todos)
    }
}
