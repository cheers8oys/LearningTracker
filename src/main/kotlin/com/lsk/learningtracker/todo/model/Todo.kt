package com.lsk.learningtracker.todo.model

import com.lsk.learningtracker.todo.enums.Priority
import com.lsk.learningtracker.todo.enums.TodoStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class Todo(
    val id: Long = 0,
    val userId: Long,
    var content: String,
    var status: TodoStatus = TodoStatus.PENDING,
    var priority: Priority = Priority.MEDIUM,
    var timerSeconds: Int = 0,
    var completedAt: LocalDateTime? = null,
    val createdDate: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(content.trim().isNotEmpty()) {
            "투두 내용을 입력해주세요."
        }
        require(content.trim().length <= 100) {
            "투두 내용은 100자 이하여야 합니다."
        }
    }
}