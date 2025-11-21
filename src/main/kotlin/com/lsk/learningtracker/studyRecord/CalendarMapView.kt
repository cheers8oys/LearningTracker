package com.lsk.learningtracker.calendar.view

import com.lsk.learningtracker.studyRecord.model.StudyRecord
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import java.time.LocalDate
import java.time.YearMonth

class CalendarMapView(
    private val onDateClick: (LocalDate) -> Unit
) {
    private val cellSize = 45.0
    private val cellSpacing = 5.0

    fun createCalendar(currentMonth: YearMonth, studyRecords: Map<LocalDate, StudyRecord>): VBox {
        val calendar = VBox(10.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER
            style = "-fx-background-color: #f5f5f5; -fx-background-radius: 10;"
            prefWidth = 400.0
        }

        val header = createHeader(currentMonth)
        val weekDaysRow = createWeekDaysRow()
        val datesGrid = createDatesGrid(currentMonth, studyRecords)

        calendar.children.addAll(header, weekDaysRow, datesGrid)
        return calendar
    }

    private fun createHeader(currentMonth: YearMonth): HBox {
        return HBox(10.0).apply {
            alignment = Pos.CENTER
            children.add(
                Label("${currentMonth.year}년 ${currentMonth.monthValue}월").apply {
                    style = "-fx-font-size: 20px; -fx-font-weight: bold;"
                }
            )
        }
    }

    private fun createWeekDaysRow(): GridPane {
        val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
        return GridPane().apply {
            hgap = cellSpacing
            vgap = cellSpacing
            alignment = Pos.CENTER

            weekDays.forEachIndexed { index, day ->
                val dayLabel = Label(day).apply {
                    style = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #666;"
                    prefWidth = cellSize
                    prefHeight = 30.0
                    alignment = Pos.CENTER
                }
                add(dayLabel, index, 0)
            }
        }
    }

    private fun createDatesGrid(currentMonth: YearMonth, studyRecords: Map<LocalDate, StudyRecord>): GridPane {
        val grid = GridPane().apply {
            hgap = cellSpacing
            vgap = cellSpacing
            alignment = Pos.CENTER
        }

        val firstDay = currentMonth.atDay(1)
        val lastDay = currentMonth.atEndOfMonth()
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7

        var currentDate = firstDay
        var row = 0
        var col = firstDayOfWeek

        while (!currentDate.isAfter(lastDay)) {
            val date = currentDate
            val cell = createDateCell(date, studyRecords[date])
            grid.add(cell, col, row)

            col++
            if (col > 6) {
                col = 0
                row++
            }
            currentDate = currentDate.plusDays(1)
        }

        return grid
    }

    private fun createDateCell(date: LocalDate, studyRecord: StudyRecord?): StackPane {
        val isToday = date == LocalDate.now()
        val isPast = date.isBefore(LocalDate.now())
        val isFuture = date.isAfter(LocalDate.now())
        val hasCompletedTodo = studyRecord != null && studyRecord.completedTodoCount > 0

        return StackPane().apply {
            prefWidth = cellSize
            prefHeight = cellSize

            val bgColor = when {
                hasCompletedTodo -> getHeatmapColorByCompletedCount(studyRecord!!.completedTodoCount)
                else -> "#ffffff"
            }

            style = """
                -fx-background-color: $bgColor;
                -fx-background-radius: 8;
                -fx-border-color: #ddd;
                -fx-border-width: 1;
                -fx-border-radius: 8;
                -fx-cursor: ${if (isPast || isToday) "hand" else "default"};
            """

            val cellContent = VBox(2.0).apply {
                alignment = Pos.CENTER

                when {
                    isToday -> {
                        children.add(
                            Label("Today").apply {
                                style = "-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: #f44336;"
                            }
                        )
                    }
                }

                children.add(
                    Label(date.dayOfMonth.toString()).apply {
                        style = "-fx-font-size: 14px; -fx-text-fill: ${if (hasCompletedTodo) "white" else "#333"};"
                    }
                )
            }

            children.add(cellContent)

            when {
                isPast || isToday -> {
                    setOnMouseClicked {
                        onDateClick(date)
                    }
                    setOnMouseEntered {
                        style = style + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
                    }
                    setOnMouseExited {
                        style = style.replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);", "")
                    }
                }
            }
        }
    }

    private fun getHeatmapColorByCompletedCount(completedCount: Int): String {
        return when {
            completedCount == 0 -> "#ffffff"
            completedCount in 1..2 -> "#c6e48b"
            completedCount in 3..4 -> "#7bc96f"
            completedCount in 5..7 -> "#239a3b"
            else -> "#196127"
        }
    }
}
