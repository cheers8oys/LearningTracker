package com.lsk.learningtracker.todo.timer

class ActiveTimerManager {
    private var activeTimerId: Long? = null

    fun canStartTimer(todoId: Long): Boolean {
        val currentActiveId = activeTimerId
        return when {
            currentActiveId == null -> true
            currentActiveId == todoId -> true
            else -> false
        }
    }

    fun startTimer(todoId: Long) {
        activeTimerId = todoId
    }

    fun stopTimer(todoId: Long) {
        when {
            activeTimerId == todoId -> activeTimerId = null
        }
    }

    fun getActiveTimerId(): Long? {
        return activeTimerId
    }

    fun hasActiveTimer(): Boolean {
        return activeTimerId != null
    }

    fun isTimerActive(todoId: Long): Boolean {
        return activeTimerId == todoId
    }
}
