package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.model.TodoStatus
import com.lsk.learningtracker.todo.model.TodoTimerManager
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox

class TodoListCell(
    private val onTimerStart: (Todo) -> Unit,
    private val onTimerPause: (Todo, Int) -> Unit,
    private val onTimerReset: (Todo) -> Unit,
    private val onComplete: (Todo) -> Unit,
    private val onEdit: (Todo) -> Unit,
    private val onDelete: (Todo) -> Unit
) : ListCell<Todo>() {

    private val label = Label()
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

    private var timerManager: TodoTimerManager? = null

    init {
        hbox.alignment = Pos.CENTER_LEFT
        hbox.children.addAll(
            label,
            timerLabel,
            startPauseButton,
            resetButton,
            completeButton,
            editButton,
            deleteButton
        )

        setupButtonHandlers()
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
        if (manager.isRunning()) {
            val elapsed = manager.pause()
            onTimerPause(todo, elapsed)
        }
        onComplete(todo)
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
    }

    private fun renderCell(todo: Todo) {
        label.text = "[${todo.status}] ${todo.content}"
        graphic = hbox

        initializeTimerIfNeeded(todo)
        updateButtonStates(todo)
    }

    private fun initializeTimerIfNeeded(todo: Todo) {
        val currentManager = timerManager

        when {
            currentManager == null -> createNewTimer(todo)
            currentManager.isRunning() -> {}
            else -> {
                currentManager.setElapsedSeconds(todo.timerSeconds)
            }
        }
    }

    private fun createNewTimer(todo: Todo) {
        timerManager = TodoTimerManager(timerLabel) { }
        timerManager?.setElapsedSeconds(todo.timerSeconds)
    }

    private fun updateButtonStates(todo: Todo) {
        val isCompleted = todo.status == TodoStatus.COMPLETED

        completeButton.isDisable = isCompleted
        startPauseButton.isDisable = isCompleted
        resetButton.isDisable = isCompleted
        editButton.isDisable = isCompleted

        when {
            isCompleted -> stopTimerIfRunning()
        }
    }

    private fun stopTimerIfRunning() {
        timerManager?.stop()
        startPauseButton.text = "시작"
    }
}
