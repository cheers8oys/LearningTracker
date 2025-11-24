package com.lsk.learningtracker.studyRecord.view

import com.lsk.learningtracker.study.service.StudyRecordService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.time.LocalDate
import java.time.YearMonth

class CalendarView(
    private val userId: Long,
    private val studyRecordService: StudyRecordService,
    private val onDateClick: (LocalDate) -> Unit
) {
    private lateinit var calendarView: CalendarMapView
    private lateinit var calendarContainer: VBox
    private var currentMonth = YearMonth.now()

    fun create(): VBox {
        initializeCalendarView()
        return createPanel()
    }

    fun refresh() {
        refreshCalendar()
    }

    fun refreshForDate(date: LocalDate) {
        currentMonth = YearMonth.from(date)
        refreshCalendar()
    }

    private fun initializeCalendarView() {
        calendarView = CalendarMapView { date ->
            onDateClick(date)
        }
    }

    private fun createPanel(): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER
            prefWidth = 450.0
            style = "-fx-background-color: white;"

            calendarContainer = VBox()
            val monthNav = createMonthNavigation()

            children.addAll(
                createHeader(),
                monthNav,
                calendarContainer
            )
        }
    }

    private fun createHeader(): Label {
        return Label("📅 학습 캘린더").apply {
            style = "-fx-font-size: 20px; -fx-font-weight: bold;"
        }
    }

    private fun createMonthNavigation(): HBox {
        val prevButton = Button("◀").apply {
            setOnAction {
                currentMonth = currentMonth.minusMonths(1)
                refreshCalendar()
            }
        }

        val todayButton = Button("오늘").apply {
            setOnAction {
                currentMonth = YearMonth.now()
                refreshCalendar()
            }
        }

        val nextButton = Button("▶").apply {
            setOnAction {
                currentMonth = currentMonth.plusMonths(1)
                refreshCalendar()
            }
        }

        return HBox(10.0, prevButton, todayButton, nextButton).apply {
            alignment = Pos.CENTER
        }
    }

    private fun refreshCalendar() {
        val startDate = currentMonth.atDay(1)
        val endDate = currentMonth.atEndOfMonth()

        val recordMap = studyRecordService.getOrCalculateStudyRecords(
            userId,
            startDate,
            endDate
        )

        val calendar = calendarView.createCalendar(currentMonth, recordMap)
        calendarContainer.children.clear()
        calendarContainer.children.add(calendar)
    }
}