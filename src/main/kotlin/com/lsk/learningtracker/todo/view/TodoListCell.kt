package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.todo.enums.Priority
import com.lsk.learningtracker.todo.enums.TodoStatus
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.timer.TimerException
import com.lsk.learningtracker.todo.timer.TodoTimerManager
import com.lsk.learningtracker.todo.view.style.TodoCellStyleManager
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox

class TodoListCell(
    private val onTimerStart: (Todo) -> Unit,
    private val onTimerPause: (Todo, Int) -> Unit,
    private val onTimerReset: (Todo) -> Unit,
    private val onComplete: (Todo) -> Unit,
    private val onEdit: (Todo) -> Unit,
    private val onDelete: (Todo) -> Unit,
    private val onCanStartTimer: (Todo) -> Boolean,
    private val onPriorityChange: (Todo, Priority) -> Unit,
    private val getActiveTimerId: () -> Long?
) : ListCell<Todo>() {

    private val priorityLabel = Label()
    private val label = Label()
    private val timerLabel = Label()
    private val priorityChoice = ChoiceBox<Priority>().apply {
        items.addAll(Priority.HIGH, Priority.MEDIUM, Priority.LOW)
        prefWidth = 85.0
    }
    private val startPauseButton = Button("시작")
    private val resetButton = Button("초기화")
    private val completeButton = Button("완료")
    private val editButton = Button("수정")
    private val deleteButton = Button("삭제")
    private val hbox = HBox(10.0)

    private val styleManager = TodoCellStyleManager()
    private val priorityStyleManager = PriorityStyleManager()
    private var timerManager: TodoTimerManager? = null

    init {
        hbox.alignment = Pos.CENTER_LEFT
        hbox.children.addAll(
            priorityLabel,
            label,
            timerLabel,
            priorityChoice,
            startPauseButton,
            resetButton,
            completeButton,
            editButton,
            deleteButton
        )

        applyInitialStyles()
        setupButtonHandlers()
        setupPriorityChoiceHandler()
    }

    private fun applyInitialStyles() {
        styleManager.applyCompleteButtonStyle(completeButton)
        styleManager.applyDeleteButtonStyle(deleteButton)
    }

    private fun setupPriorityChoiceHandler() {
        priorityChoice.setOnAction {
            val currentItem = item ?: return@setOnAction
            val selectedPriority = priorityChoice.value ?: return@setOnAction

            when {
                selectedPriority != currentItem.priority -> {
                    onPriorityChange(currentItem, selectedPriority)
                }
            }
        }
    }

    private fun setupButtonHandlers() {
        startPauseButton.setOnAction {
            val currentItem = item ?: return@setOnAction
            val manager = timerManager ?: return@setOnAction

            when {
                manager.isRunning() -> handlePause(currentItem, manager)
                else -> handleStart(currentItem, manager)
            }
        }

        resetButton.setOnAction {
            val currentItem = item ?: return@setOnAction
            val manager = timerManager ?: return@setOnAction

            manager.reset()
            startPauseButton.text = "시작"
            onTimerReset(currentItem)
        }

        completeButton.setOnAction {
            val currentItem = item ?: return@setOnAction
            handleComplete(currentItem)
        }

        editButton.setOnAction {
            val currentItem = item ?: return@setOnAction
            onEdit(currentItem)
        }

        deleteButton.setOnAction {
            val currentItem = item ?: return@setOnAction
            onDelete(currentItem)
        }
    }

    private fun handleStart(todo: Todo, manager: TodoTimerManager) {
        when {
            !onCanStartTimer(todo) -> {
                showWarning("다른 타이머가 실행 중입니다", "하나의 타이머만 실행할 수 있습니다.")
                return
            }
        }

        manager.start()
        startPauseButton.text = "일시정지"
        onTimerStart(todo)
    }

    private fun handlePause(todo: Todo, manager: TodoTimerManager) {
        val elapsed = manager.pause()
        startPauseButton.text = "시작"
        onTimerPause(todo, elapsed)
    }

    private fun handleComplete(todo: Todo) {
        val manager = timerManager ?: return

        val finalElapsed = when {
            manager.isRunning() -> {
                val elapsed = manager.pause()
                startPauseButton.text = "시작"
                elapsed
            }
            else -> manager.getElapsedSeconds()
        }

        onTimerPause(todo, finalElapsed)
        onComplete(todo)
    }

    private fun showWarning(title: String, message: String) {
        val alert = Alert(Alert.AlertType.WARNING)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    private fun showError(title: String, message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    override fun updateItem(item: Todo?, empty: Boolean) {
        super.updateItem(item, empty)

        when {
            empty || item == null -> clearCell()
            else -> renderCell(item)
        }
    }

    private fun clearCell() {
        text = null
        graphic = null
        timerManager?.stop()
        timerManager = null
        startPauseButton.text = "시작"
    }

    private fun renderCell(todo: Todo) {
        val priorityIcon = priorityStyleManager.getPriorityIcon(todo.priority)
        priorityLabel.text = priorityIcon
        priorityStyleManager.applyPriorityStyle(priorityLabel, todo.priority)

        label.text = "[${todo.status}] ${todo.content}"
        graphic = hbox

        priorityChoice.value = todo.priority

        styleManager.applyStatusStyle(label, todo.status)
        initializeTimerIfNeeded(todo)
        updateButtonStates(todo)
        updateButtonText()
    }

    private fun initializeTimerIfNeeded(todo: Todo) {
        val currentManager = timerManager
        val activeTimerId = getActiveTimerId()

        when {
            currentManager == null || currentManager.getTodoId() != todo.id -> {
                currentManager?.stop()
                createNewTimer(todo)

                if (activeTimerId == todo.id) {
                    timerManager?.start()
                }
            }
            currentManager.isRunning() -> {
            }
            else -> {
                currentManager.setElapsedSeconds(todo.timerSeconds)

                if (activeTimerId == todo.id) {
                    currentManager.start()
                }
            }
        }
    }

    private fun createNewTimer(todo: Todo) {
        timerManager = TodoTimerManager(
            todoId = todo.id,
            timerLabel = timerLabel,
            onTimerUpdate = { },
            onTimerStateChange = { isRunning ->
                updateTimerStyle(isRunning)
                updateButtonText()
            },
            onError = { exception -> handleTimerError(exception) }
        )
        timerManager?.setElapsedSeconds(todo.timerSeconds)
        updateTimerStyle(false)
    }

    private fun updateButtonText() {
        val manager = timerManager
        startPauseButton.text = when {
            manager != null && manager.isRunning() -> "일시정지"
            else -> "시작"
        }
    }

    private fun handleTimerError(exception: Exception) {
        when (exception) {
            is TimerException -> showError("타이머 오류", exception.message ?: "알 수 없는 오류")
            else -> showError("오류", "타이머 작동 중 오류가 발생했습니다.")
        }
    }

    private fun updateTimerStyle(isRunning: Boolean) {
        when {
            isRunning -> styleManager.applyTimerRunningStyle(timerLabel, startPauseButton)
            else -> styleManager.applyTimerStoppedStyle(timerLabel, startPauseButton)
        }
    }

    private fun updateButtonStates(todo: Todo) {
        val isCompleted = todo.status == TodoStatus.COMPLETED

        completeButton.isDisable = isCompleted
        startPauseButton.isDisable = isCompleted
        resetButton.isDisable = isCompleted
        editButton.isDisable = isCompleted
        priorityChoice.isDisable = isCompleted

        when {
            isCompleted -> {
                stopTimerIfRunning()
                applyCompletedStyles()
            }
        }
    }

    private fun applyCompletedStyles() {
        styleManager.applyDisabledButtonStyle(startPauseButton)
        styleManager.applyDisabledButtonStyle(resetButton)
        styleManager.applyDisabledButtonStyle(editButton)
    }

    private fun stopTimerIfRunning() {
        timerManager?.stop()
        startPauseButton.text = "시작"
    }
}
