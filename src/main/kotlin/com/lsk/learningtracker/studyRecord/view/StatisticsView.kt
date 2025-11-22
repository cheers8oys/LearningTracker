package com.lsk.learningtracker.statistics.view

import com.lsk.learningtracker.studyRecord.model.StatisticsData
import com.lsk.learningtracker.studyRecord.service.StatisticsService
import com.lsk.learningtracker.utils.TimeFormatter
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatisticsView(
    private val userId: Long,
    private val statisticsService: StatisticsService
) {
    private var currentDate = LocalDate.now()

    private lateinit var dateLabel: Label
    private lateinit var tabPane: TabPane
    private lateinit var dailyTab: Tab
    private lateinit var weeklyTab: Tab
    private lateinit var monthlyTab: Tab

    fun show(parentStage: Stage) {
        val modal = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            initOwner(parentStage)
            title = "📊 학습 통계"
        }

        val root = createContent(modal)
        val scene = Scene(root, 700.0, 600.0)
        modal.scene = scene
        modal.show()
    }

    private fun createContent(modal: Stage): VBox {
        return VBox(20.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER

            children.addAll(
                createHeader(),
                createDateNavigation(),
                createTabPane(),
                createCloseButton(modal)
            )
        }
    }

    private fun createHeader(): Label {
        return Label("📊 학습 통계").apply {
            style = "-fx-font-size: 28px; -fx-font-weight: bold;"
        }
    }

    private fun createDateNavigation(): HBox {
        val prevButton = Button("◀ 이전").apply {
            setOnAction {
                currentDate = currentDate.minusDays(1)
                refreshStatistics()
            }
        }

        val todayButton = Button("오늘").apply {
            setOnAction {
                currentDate = LocalDate.now()
                refreshStatistics()
            }
        }

        val nextButton = Button("다음 ▶").apply {
            setOnAction {
                currentDate = currentDate.plusDays(1)
                refreshStatistics()
            }
            isDisable = currentDate >= LocalDate.now()
        }

        dateLabel = Label(formatDate(currentDate)).apply {
            style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            minWidth = 150.0
            alignment = Pos.CENTER
        }

        return HBox(10.0, prevButton, todayButton, nextButton, dateLabel).apply {
            alignment = Pos.CENTER
        }
    }

    private fun createTabPane(): TabPane {
        tabPane = TabPane().apply {
            dailyTab = createDailyTab()
            weeklyTab = createWeeklyTab()
            monthlyTab = createMonthlyTab()

            tabs.addAll(dailyTab, weeklyTab, monthlyTab)
        }
        return tabPane
    }

    private fun createDailyTab(): Tab {
        val dailyStats = statisticsService.getDailyStatistics(userId, currentDate)

        return Tab("일간 통계").apply {
            isClosable = false
            content = createStatisticsContent(dailyStats, "일간")
        }
    }

    private fun createWeeklyTab(): Tab {
        val weeklyStats = statisticsService.getWeeklyStatistics(userId, currentDate)

        return Tab("주간 통계").apply {
            isClosable = false
            content = createStatisticsContent(weeklyStats, "주간")
        }
    }

    private fun createMonthlyTab(): Tab {
        val monthlyStats = statisticsService.getMonthlyStatistics(userId, currentDate)

        return Tab("월간 통계").apply {
            isClosable = false
            content = createStatisticsContent(monthlyStats, "월간")
        }
    }

    private fun refreshStatistics() {

        dateLabel.text = formatDate(currentDate)

        val dailyStats = statisticsService.getDailyStatistics(userId, currentDate)
        val weeklyStats = statisticsService.getWeeklyStatistics(userId, currentDate)
        val monthlyStats = statisticsService.getMonthlyStatistics(userId, currentDate)

        dailyTab.content = createStatisticsContent(dailyStats, "일간")
        weeklyTab.content = createStatisticsContent(weeklyStats, "주간")
        monthlyTab.content = createStatisticsContent(monthlyStats, "월간")

        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {

        val parent = dateLabel.parent as? HBox
        parent?.children?.forEach { node ->
            if (node is Button && node.text == "다음 ▶") {
                node.isDisable = currentDate >= LocalDate.now()
            }
        }
    }

    private fun createStatisticsContent(stats: StatisticsData, period: String): VBox {
        return VBox(20.0).apply {
            padding = Insets(30.0)
            alignment = Pos.TOP_CENTER

            children.addAll(
                createStatCard("⏱️ 총 학습 시간", TimeFormatter.formatSeconds(stats.totalStudySeconds)),
                createStatCard("📝 총 투두 개수", "${stats.totalTodoCount}개"),
                createStatCard("✅ 완료한 투두", "${stats.completedTodoCount}개"),
                createStatCard("📈 완료율", String.format("%.1f%%", stats.completionRate)),
                createStatCard("📅 학습한 날", "${stats.studyDayCount}일"),
                createStatCard("⏰ 평균 학습 시간", TimeFormatter.formatSeconds(stats.averageStudySeconds))
            )
        }
    }

    private fun createStatCard(title: String, value: String): VBox {
        return VBox(5.0).apply {
            alignment = Pos.CENTER
            style = """
                -fx-background-color: #f5f5f5;
                -fx-background-radius: 10;
                -fx-padding: 20;
                -fx-min-width: 500;
            """

            children.addAll(
                Label(title).apply {
                    style = "-fx-font-size: 14px; -fx-text-fill: #666;"
                },
                Label(value).apply {
                    style = "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2196F3;"
                }
            )
        }
    }

    private fun createCloseButton(modal: Stage): Button {
        return Button("닫기").apply {
            prefWidth = 120.0
            prefHeight = 40.0
            style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;"
            setOnAction {
                modal.close()
            }
        }
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", java.util.Locale.KOREAN))
    }
}
