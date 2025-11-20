package com.lsk.learningtracker.todo.timer

sealed class TimerException(message: String) : Exception(message) {
    class AlreadyRunningException(todoId: Long) :
        TimerException("다른 타이머(ID: $todoId)가 이미 실행 중입니다.")

    class InvalidStateException(message: String) :
        TimerException(message)

    class SaveFailedException(cause: Throwable) :
        TimerException("타이머 저장에 실패했습니다: ${cause.message}")
}
