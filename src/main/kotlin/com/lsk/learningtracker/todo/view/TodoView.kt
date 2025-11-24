package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.faceDetection.controller.FaceDetectionController
import com.lsk.learningtracker.faceDetection.view.AbsenceTimerView
import com.lsk.learningtracker.todo.controller.TodoController
import com.lsk.learningtracker.todo.enums.Priority
import com.lsk.learningtracker.todo.enums.TodoFilter
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.timer.TimerException
import com.lsk.learningtracker.utils.TimeFormatter
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
    private lateinit var faceDetectionController: FaceDetectionController
    private lateinit var absenceTimerView: AbsenceTimerView
    private var faceDetectionPaused = false
    private var isFaceDetectionActive = false

    init {
        initializeFaceDetection()
    }

    fun create(): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER
            children.addAll(
                createHeader(),
                createFaceDetectionToggle(),
                createInputBox(),
                createFilterBox(),
                createTodoListView()
            )
        }
    }

    fun refresh() {
        refreshTodoList()
    }

    private fun initializeFaceDetection() {
        absenceTimerView = AbsenceTimerView(
            parentStage = stage,
            onResumeStudy = { todo, absenceTime ->
                handleResumeAfterAbsence(todo, absenceTime)
            }
        )

        faceDetectionController = FaceDetectionController(
            todoController = todoController,
            onShowAbsencePopup = { todo ->
                absenceTimerView.show(todo)
            },
            onHideAbsencePopup = {
                absenceTimerView.showResumeButtons()
            }
        )
    }

    private fun createHeader(): Label {
        return Label("환영합니다, ${username}님!").apply {
            style = "-fx-font-size: 24px; -fx-font-weight: bold;"
        }
    }

    private fun createFaceDetectionToggle(): HBox {
        val toggleLabel = Label("얼굴 감지 일시정지:").apply {
            style = "-fx-font-size: 14px; -fx-font-weight: bold;"
        }

        val statusLabel = Label("[ 활성화 ]").apply {
            style = "-fx-font-size: 12px; -fx-text-fill: green; -fx-font-weight: bold;"
        }

        val toggleCheckBox = CheckBox().apply {
            isSelected = faceDetectionPaused

            setOnAction {
                faceDetectionPaused = isSelected

                when {
                    faceDetectionPaused -> {
                        statusLabel.text = "[ 일시정지됨 ]"
                        statusLabel.style = "-fx-font-size: 12px; -fx-text-fill: red; -fx-font-weight: bold;"

                        when {
                            isFaceDetectionActive -> {
                                faceDetectionController.disable()
                                isFaceDetectionActive = false
                            }
                        }
                    }
                    else -> {
                        statusLabel.text = "[ 활성화 ]"
                        statusLabel.style = "-fx-font-size: 12px; -fx-text-fill: green; -fx-font-weight: bold;"

                        val activeTimerId = todoController.getActiveTimerId()
                        when {
                            activeTimerId != null -> {
                                val activeTodo = todoList.find { it.id == activeTimerId }
                                activeTodo?.let { todo ->
                                    faceDetectionController.enableForTodo(todo)
                                    isFaceDetectionActive = true
                                }
                            }
                        }
                    }
                }
            }
        }

        val infoLabel = Label("(체크 시 얼굴 감지 기능 일시 중지)").apply {
            style = "-fx-font-size: 11px; -fx-text-fill: gray;"
        }

        return HBox(10.0, toggleLabel, toggleCheckBox, statusLabel, infoLabel).apply {
            alignment = Pos.CENTER
            padding = Insets(5.0)
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

            when {
                !faceDetectionPaused -> {
                    try {
                        faceDetectionController.enableForTodo(todo)
                        isFaceDetectionActive = true
                    } catch (e: Exception) {
                        showError("얼굴 감지 실패", "얼굴 감지를 시작할 수 없습니다: ${e.message}")
                    }
                }
            }

            refreshTodoList()
        } catch (e: TimerException.AlreadyRunningException) {
            showError("타이머 실행 불가", e.message ?: "다른 타이머가 실행 중입니다.")
        } catch (e: Exception) {
            showError("오류 발생", "타이머 시작 중 오류가 발생했습니다: ${e.message}")
        }
    }

    private fun handleTimerPause(todo: Todo, elapsedSeconds: Int) {
        try {
            todoController.pauseTimer(todo, elapsedSeconds)

            when {
                isFaceDetectionActive -> {
                    faceDetectionController.disable()
                    isFaceDetectionActive = false
                }
            }

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
            refreshTodoList()
        } catch (e: TimerException.SaveFailedException) {
            showError("초기화 실패", "타이머 초기화에 실패했습니다.")
        }
    }

    private fun handleComplete(todo: Todo) {
        todoController.completeTodo(todo)

        when {
            isFaceDetectionActive -> {
                faceDetectionController.disable()
                isFaceDetectionActive = false
            }
        }

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
        val isActive = todoController.isTimerActive(todo)

        todoController.deleteTodo(todo)

        when {
            isActive && isFaceDetectionActive -> {
                faceDetectionController.disable()
                isFaceDetectionActive = false
            }
        }

        refreshTodoList()
        onStudyRecordUpdate()
    }

    private fun handleResumeAfterAbsence(todo: Todo, absenceTime: Int) {
        try {
            todoController.startTimer(todo)

            when {
                !faceDetectionPaused -> {
                    faceDetectionController.enableForTodo(todo)
                    isFaceDetectionActive = true
                }
            }

            showAbsenceReport(absenceTime)
            refreshTodoList()
        } catch (e: TimerException.AlreadyRunningException) {
            showError("타이머 재개 실패", "타이머를 재개할 수 없습니다.")
        }
    }

    private fun showAbsenceReport(absenceTime: Int) {
        val formattedTime = TimeFormatter.formatSecondsWithLabel(absenceTime)
        showInfo("자리 비움 시간", "자리를 비운 시간: $formattedTime")
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

    private fun showInfo(title: String, message: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    fun cleanup() {
        when {
            isFaceDetectionActive -> {
                faceDetectionController.disable()
                isFaceDetectionActive = false
            }
        }

        absenceTimerView.close()
    }
}
