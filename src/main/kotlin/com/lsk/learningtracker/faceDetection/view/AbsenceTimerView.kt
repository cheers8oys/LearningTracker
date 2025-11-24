package com.lsk.learningtracker.faceDetection.view

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.utils.TimeFormatter
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

class AbsenceTimerView(
    private val parentStage: Stage,
    private val onResumeStudy: (Todo, Int) -> Unit
) {
    private var stage: Stage? = null
    private var absenceSeconds = 0
    private var animationTimer: AnimationTimer? = null
    private var startTimeNs: Long = 0L
    private var isBlinking = false
    private val timerLabel = Label("00:00")
    private val messageLabel = Label("자리를 비우신 것 같습니다")
    private val resumeButton = Button("예")
    private val cancelButton = Button("아니오")
    private var currentTodo: Todo? = null

    fun show(todo: Todo) {
        currentTodo = todo
        absenceSeconds = 0
        createStage()
        startAbsenceTimer()
        stage?.show()
    }

    private fun createStage() {
        stage = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            initOwner(parentStage)
            initStyle(StageStyle.UTILITY)
            title = "자리 비움 감지"

            scene = Scene(createContent(), 400.0, 250.0)
            isResizable = false

            // 창이 닫힐 때 리소스 정리
            setOnCloseRequest {
                stopAbsenceTimer()
                currentTodo = null
            }
        }
    }

    private fun createContent(): VBox {
        return VBox(20.0).apply {
            padding = Insets(30.0)
            alignment = Pos.CENTER

            children.addAll(
                messageLabel.apply {
                    style = "-fx-font-size: 16px; -fx-font-weight: bold;"
                },
                Label("자리 비움 시간:").apply {
                    style = "-fx-font-size: 14px;"
                },
                timerLabel.apply {
                    style = "-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: red;"
                },
                createButtonBox()
            )
        }
    }

    private fun createButtonBox(): VBox {
        val buttonLabel = Label("다시 학습을 시작하시겠습니까?").apply {
            style = "-fx-font-size: 14px;"
        }

        val buttons = javafx.scene.layout.HBox(15.0).apply {
            alignment = Pos.CENTER
            children.addAll(
                resumeButton.apply {
                    prefWidth = 100.0
                    style = "-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white;"
                    setOnAction { handleResume() }
                },
                cancelButton.apply {
                    prefWidth = 100.0
                    style = "-fx-font-size: 14px; -fx-background-color: #f44336; -fx-text-fill: white;"
                    setOnAction { handleCancel() }
                }
            )
        }

        return VBox(10.0).apply {
            alignment = Pos.CENTER
            children.addAll(buttonLabel, buttons)
            isVisible = false
        }
    }

    private fun startAbsenceTimer() {
        startTimeNs = System.nanoTime()

        animationTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                val delta = (now - startTimeNs) / 1_000_000_000
                absenceSeconds = delta.toInt()
                updateTimerDisplay()
                updateBlinking()
            }
        }.also { it.start() }
    }

    private fun updateTimerDisplay() {
        timerLabel.text = TimeFormatter.formatSeconds(absenceSeconds)
    }

    private fun updateBlinking() {
        isBlinking = !isBlinking
        val opacity = when {
            isBlinking -> 0.3
            else -> 1.0
        }
        timerLabel.opacity = opacity
    }

    fun showResumeButtons() {
        stopBlinking()
        (stage?.scene?.root as? VBox)?.children?.last()?.isVisible = true
    }

    private fun stopBlinking() {
        timerLabel.opacity = 1.0
        isBlinking = false
    }

    private fun handleResume() {
        val todo = currentTodo
        val savedAbsenceTime = absenceSeconds

        when {
            todo == null -> {
                close()
                return
            }
        }

        // 1단계: 버튼 비활성화 (중복 클릭 방지)
        resumeButton.isDisable = true
        cancelButton.isDisable = true

        // 2단계: 타이머 정지
        stopAbsenceTimer()

        // 3단계: 팝업 닫기 (Platform.runLater로 확실하게)
        Platform.runLater {
            close()

            // 4단계: 닫힌 후 재개 로직 실행
            Platform.runLater {
                if (todo != null) {
                    onResumeStudy(todo, savedAbsenceTime)
                }
            }
        }
    }

    private fun handleCancel() {
        stopAbsenceTimer()
        close()
    }

    private fun stopAbsenceTimer() {
        animationTimer?.stop()
        animationTimer = null
    }

    fun close() {
        stopAbsenceTimer()

        val currentStage = stage
        stage = null
        currentTodo = null

        // Platform.runLater로 UI 스레드에서 확실하게 닫기
        Platform.runLater {
            currentStage?.close()
        }
    }
}
