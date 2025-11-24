package com.lsk.learningtracker.faceDetection.controller

import com.lsk.learningtracker.faceDetection.service.FaceDetectionService
import com.lsk.learningtracker.todo.controller.TodoController
import com.lsk.learningtracker.todo.model.Todo
import javafx.application.Platform

class FaceDetectionController(
    private val todoController: TodoController,
    private val onShowAbsencePopup: (Todo) -> Unit,
    private val onHideAbsencePopup: () -> Unit
) {
    private var faceDetectionService: FaceDetectionService? = null
    private var currentTodo: Todo? = null
    private var isAbsent = false
    private var lastReturnTime: Long = 0  // 마지막 복귀 시간
    private val cooldownMillis: Long = 10000  // 10초 쿨다운

    fun enableForTodo(todo: Todo) {
        currentTodo = todo
        startFaceDetection()
    }

    fun disable() {
        stopFaceDetection()
        currentTodo = null
        isAbsent = false
        lastReturnTime = 0
    }

    private fun startFaceDetection() {
        faceDetectionService = FaceDetectionService(
            onFaceDetected = { handleFaceDetected() },
            onFaceLost = { handleFaceLost() },
            onError = { error -> handleError(error) }
        )
        faceDetectionService?.start()
    }

    private fun handleFaceDetected() {
        Platform.runLater {
            when {
                isAbsent -> {
                    isAbsent = false
                    lastReturnTime = System.currentTimeMillis()  // 복귀 시간 기록
                    onHideAbsencePopup()
                }
            }
        }
    }

    private fun handleFaceLost() {
        Platform.runLater {
            val todo = currentTodo ?: return@runLater

            // 쿨다운 체크: 복귀 후 10초 이내면 무시
            val currentTime = System.currentTimeMillis()
            val timeSinceReturn = currentTime - lastReturnTime

            when {
                timeSinceReturn < cooldownMillis -> {
                    // 복귀한지 10초 이내 - 무시
                    return@runLater
                }
                isAbsent -> {
                    // 이미 부재 상태 - 무시
                    return@runLater
                }
                else -> {
                    // 정상적인 부재 감지
                    isAbsent = true
                    pauseTodoTimer(todo)
                    onShowAbsencePopup(todo)
                }
            }
        }
    }

    private fun pauseTodoTimer(todo: Todo) {
        when {
            todoController.isTimerActive(todo) -> {
                todoController.pauseTimer(todo)
            }
        }
    }

    private fun handleError(error: String) {
        Platform.runLater {
            stopFaceDetection()
        }
    }

    private fun stopFaceDetection() {
        faceDetectionService?.stop()
        faceDetectionService = null
    }
}
