package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.todo.enums.Priority
import javafx.scene.control.Label

class PriorityStyleManager {

    fun applyPriorityStyle(label: Label, priority: Priority) {
        label.style = when (priority) {
            Priority.HIGH -> STYLE_HIGH
            Priority.MEDIUM -> STYLE_MEDIUM
            Priority.LOW -> STYLE_LOW
        }
    }

    fun getPriorityIcon(priority: Priority): String {
        return when (priority) {
            Priority.HIGH -> "⬆"
            Priority.MEDIUM -> "➡"
            Priority.LOW -> "⬇"
        }
    }

    companion object {
        private const val STYLE_HIGH = "-fx-text-fill: #F44336; -fx-font-weight: bold;"
        private const val STYLE_MEDIUM = "-fx-text-fill: #FF9800;"
        private const val STYLE_LOW = "-fx-text-fill: #9E9E9E;"
    }
}