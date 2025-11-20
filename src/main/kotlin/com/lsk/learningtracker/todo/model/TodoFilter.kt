package com.lsk.learningtracker.todo.filter

enum class TodoFilter(val displayName: String) {
    ALL("전체"),
    PENDING("대기"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료")
}
