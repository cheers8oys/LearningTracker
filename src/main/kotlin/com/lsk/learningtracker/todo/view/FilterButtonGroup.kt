package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.todo.enums.TodoFilter
import javafx.geometry.Pos
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox

class FilterButtonGroup(
    private val onFilterChange: (TodoFilter) -> Unit
) {
    private val toggleGroup = ToggleGroup()
    private val filterButtons = mutableMapOf<TodoFilter, ToggleButton>()

    fun createFilterBox(): HBox {
        val allButton = createFilterButton(TodoFilter.ALL, true)
        val pendingButton = createFilterButton(TodoFilter.PENDING, false)
        val inProgressButton = createFilterButton(TodoFilter.IN_PROGRESS, false)
        val completedButton = createFilterButton(TodoFilter.COMPLETED, false)

        filterButtons[TodoFilter.ALL] = allButton
        filterButtons[TodoFilter.PENDING] = pendingButton
        filterButtons[TodoFilter.IN_PROGRESS] = inProgressButton
        filterButtons[TodoFilter.COMPLETED] = completedButton

        setupToggleGroupListener()

        return HBox(5.0, allButton, pendingButton, inProgressButton, completedButton).apply {
            alignment = Pos.CENTER
        }
    }

    private fun createFilterButton(filter: TodoFilter, selected: Boolean): ToggleButton {
        return ToggleButton(filter.displayName).apply {
            toggleGroup = this@FilterButtonGroup.toggleGroup
            isSelected = selected
            prefWidth = 80.0

            setOnAction {
                when {
                    isSelected -> {
                        onFilterChange(filter)
                    }
                    else -> {
                        isSelected = true
                    }
                }
            }

            when {
                selected -> applySelectedStyle()
                else -> applyDefaultStyle()
            }
        }
    }

    private fun setupToggleGroupListener() {
        toggleGroup.selectedToggleProperty().addListener { _, _, newToggle ->
            updateAllButtonStyles()
        }
    }

    private fun updateAllButtonStyles() {
        filterButtons.forEach { (_, button) ->
            when {
                button.isSelected -> button.applySelectedStyle()
                else -> button.applyDefaultStyle()
            }
        }
    }

    private fun ToggleButton.applySelectedStyle() {
        style = STYLE_SELECTED
    }

    private fun ToggleButton.applyDefaultStyle() {
        style = STYLE_DEFAULT
    }

    fun selectFilter(filter: TodoFilter) {
        filterButtons[filter]?.let { button ->
            button.isSelected = true
            updateAllButtonStyles()
        }
    }

    companion object {
        private const val STYLE_SELECTED =
            "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
        private const val STYLE_DEFAULT =
            "-fx-background-color: #EEEEEE; -fx-text-fill: #333333;"
    }
}
