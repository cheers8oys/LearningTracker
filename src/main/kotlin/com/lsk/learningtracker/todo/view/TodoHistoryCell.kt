package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.todo.enums.TodoStatus
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.utils.TimeFormatter
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority as LayoutPriority

class TodoHistoryCell : ListCell<Todo>() {

    private val priorityStyleManager = PriorityStyleManager()

    override fun updateItem(item: Todo?, empty: Boolean) {
        super.updateItem(item, empty)

        when {
            empty || item == null -> {
                text = null
                graphic = null
            }
            else -> {
                graphic = createCellContent(item)
            }
        }
    }

    private fun createCellContent(todo: Todo): HBox {
        val priorityIcon = Label(priorityStyleManager.getPriorityIcon(todo.priority)).apply {
            priorityStyleManager.applyPriorityStyle(this, todo.priority)
            style = style + "-fx-font-size: 16px;"
        }

        val contentLabel = Label(todo.content).apply {
            style = when (todo.status) {
                TodoStatus.COMPLETED ->
                    "-fx-text-fill: #9E9E9E; -fx-strikethrough: true; -fx-font-size: 14px;"
                else -> "-fx-font-size: 14px;"
            }
            maxWidth = Double.MAX_VALUE
            HBox.setHgrow(this, LayoutPriority.ALWAYS)
        }

        val statusLabel = Label("[${todo.status}]").apply {
            style = when (todo.status) {
                TodoStatus.COMPLETED -> "-fx-text-fill: #4CAF50;"
                TodoStatus.IN_PROGRESS -> "-fx-text-fill: #2196F3;"
                else -> "-fx-text-fill: #666;"
            }
        }

        val timerLabel = Label(TimeFormatter.formatSeconds(todo.timerSeconds)).apply {
            style = "-fx-font-size: 14px; -fx-text-fill: #FF5722; -fx-font-weight: bold;"
        }

        return HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = javafx.geometry.Insets(5.0)
            children.addAll(priorityIcon, contentLabel, statusLabel, timerLabel)
        }
    }
}
