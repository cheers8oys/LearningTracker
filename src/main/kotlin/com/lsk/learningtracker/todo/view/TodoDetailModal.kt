package com.lsk.learningtracker.todo.view

import com.lsk.learningtracker.studyRecord.model.StudyRecord
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.utils.TimeFormatter
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodoDetailModal {

    fun show(parentStage: Stage, date: LocalDate, todos: List<Todo>, studyRecord: StudyRecord?) {
        val modal = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            initOwner(parentStage)
            title = "${date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))} 학습 기록"
        }

        val root = createModalContent(date, todos, studyRecord, modal)
        val scene = Scene(root, 600.0, 500.0)
        modal.scene = scene
        modal.show()
    }

    private fun createModalContent(date: LocalDate, todos: List<Todo>, studyRecord: StudyRecord?, modal: Stage): VBox {
        return VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER

            children.addAll(
                createHeader(date),
                createSummary(studyRecord, todos),
                createTodoList(todos),
                createCloseButton(modal)
            )
        }
    }

    private fun createHeader(date: LocalDate): Label {
        return Label(date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", java.util.Locale.KOREAN))).apply {
            style = "-fx-font-size: 24px; -fx-font-weight: bold;"
        }
    }

    private fun createSummary(studyRecord: StudyRecord?, todos: List<Todo>): VBox {
        val totalSeconds = studyRecord?.totalStudySeconds ?: 0
        val completedCount = studyRecord?.completedTodoCount ?: 0
        val totalCount = studyRecord?.totalTodoCount ?: todos.size

        return VBox(5.0).apply {
            alignment = Pos.CENTER
            style = "-fx-background-color: #f0f0f0; -fx-padding: 15; -fx-background-radius: 10;"

            children.addAll(
                Label("📚 총 학습 시간: ${TimeFormatter.formatSeconds(totalSeconds)}").apply {
                    style = "-fx-font-size: 16px; -fx-font-weight: bold;"
                },
                Label("✅ 완료한 투두: $completedCount / $totalCount").apply {
                    style = "-fx-font-size: 14px;"
                }
            )
        }
    }

    private fun createTodoList(todos: List<Todo>): ListView<Todo> {
        return ListView<Todo>().apply {
            items.addAll(todos)
            prefHeight = 300.0
            cellFactory = Callback {
                TodoHistoryCell()
            }
        }
    }

    private fun createCloseButton(modal: Stage): Button {
        return Button("닫기").apply {
            prefWidth = 100.0
            style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
            setOnAction {
                modal.close()
            }
        }
    }
}
