package com.lsk.learningtracker.todo.view.style

import com.lsk.learningtracker.todo.model.TodoStatus
import javafx.scene.control.Button
import javafx.scene.control.Label

class TodoCellStyleManager {

    fun applyStatusStyle(label: Label, status: TodoStatus) {
        label.style = when (status) {
            TodoStatus.PENDING -> STYLE_PENDING
            TodoStatus.IN_PROGRESS -> STYLE_IN_PROGRESS
            TodoStatus.COMPLETED -> STYLE_COMPLETED
        }
    }

    fun applyTimerRunningStyle(timerLabel: Label, button: Button) {
        timerLabel.style = STYLE_TIMER_RUNNING
        button.style = STYLE_BUTTON_PAUSE
    }

    fun applyTimerStoppedStyle(timerLabel: Label, button: Button) {
        timerLabel.style = STYLE_TIMER_STOPPED
        button.style = STYLE_BUTTON_START
    }

    fun applyCompleteButtonStyle(button: Button) {
        button.style = STYLE_BUTTON_COMPLETE
    }

    fun applyDeleteButtonStyle(button: Button) {
        button.style = STYLE_BUTTON_DELETE
    }

    fun applyDisabledButtonStyle(button: Button) {
        button.style = STYLE_BUTTON_DISABLED
    }

    companion object {
        private const val STYLE_PENDING = "-fx-text-fill: #666666;"
        private const val STYLE_IN_PROGRESS = "-fx-text-fill: #2196F3; -fx-font-weight: bold;"
        private const val STYLE_COMPLETED = "-fx-text-fill: #9E9E9E; -fx-strikethrough: true;"

        private const val STYLE_TIMER_RUNNING =
            "-fx-text-fill: #FF5722; -fx-font-weight: bold; -fx-font-size: 14px;"
        private const val STYLE_TIMER_STOPPED =
            "-fx-text-fill: #333333; -fx-font-size: 14px;"

        private const val STYLE_BUTTON_START =
            "-fx-background-color: #2196F3; -fx-text-fill: white;"
        private const val STYLE_BUTTON_PAUSE =
            "-fx-background-color: #FF9800; -fx-text-fill: white;"
        private const val STYLE_BUTTON_COMPLETE =
            "-fx-background-color: #4CAF50; -fx-text-fill: white;"
        private const val STYLE_BUTTON_DELETE =
            "-fx-background-color: #f44336; -fx-text-fill: white;"
        private const val STYLE_BUTTON_DISABLED =
            "-fx-background-color: #CCCCCC; -fx-text-fill: #666666;"
    }
}
