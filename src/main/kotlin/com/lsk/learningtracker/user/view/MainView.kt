package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.model.TodoStatus
import com.lsk.learningtracker.todo.service.TodoService
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
            prefWidth = 500.0
            prefHeight = 400.0
            cellFactory = javafx.util.Callback {
                object : ListCell<Todo>() {
                    private val completeButton = Button("완료").apply {
                        style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
                    }
                    private val editButton = Button("수정")
                    private val deleteButton = Button("삭제").apply {
                        style = "-fx-background-color: #f44336; -fx-text-fill: white;"
                    }
                    private val hbox = HBox(10.0)
                    private val label = Label()

                    init {
                        hbox.alignment = Pos.CENTER_LEFT
                        hbox.children.addAll(label, completeButton, editButton, deleteButton)
                    }

                    override fun updateItem(item: Todo?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty || item == null) {
                            text = null
                            graphic = null
                        } else {
                            label.text = "[${item.status}] ${item.content}"
                            graphic = hbox

                            completeButton.isDisable = item.status == TodoStatus.COMPLETED
                            completeButton.setOnAction {
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

        val scene = Scene(root, 550.0, 600.0)
        stage.scene = scene
        stage.title = "학습 Tracker - 투두리스트"
        stage.show()
    }

    private fun refreshTodoList() {
        val todos = todoService.getTodayTodos(userId = user.username.hashCode().toLong())
        todoList.setAll(todos)
    }
}
