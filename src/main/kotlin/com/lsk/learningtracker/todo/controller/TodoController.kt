package com.lsk.learningtracker.todo.controller

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.model.TodoStatus
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.todo.timer.ActiveTimerManager
import com.lsk.learningtracker.todo.timer.TimerException
import java.time.LocalDateTime

class TodoController(
    private val userId: Long,
    private val todoService: TodoService,
    private val activeTimerManager: ActiveTimerManager
) {
    fun getTodayTodos(): List<Todo> {
        return todoService.getTodayTodos(userId)
    }

    fun createTodo(content: String): Todo {
        return todoService.createTodo(userId, content)
    }

    fun updateTodoContent(todo: Todo, newContent: String) {
        todo.content = newContent.trim()
        todoService.updateTodo(todo)
    }

    fun deleteTodo(todo: Todo) {
        when {
            activeTimerManager.isTimerActive(todo.id) -> {
                activeTimerManager.stopTimer(todo.id)
            }
        }
        todoService.deleteTodo(todo.id)
    }

    fun completeTodo(todo: Todo) {
        when {
            activeTimerManager.isTimerActive(todo.id) -> {
                activeTimerManager.stopTimer(todo.id)
            }
        }
        todo.status = TodoStatus.COMPLETED
        todo.completedAt = LocalDateTime.now()
        todoService.updateTodo(todo)
    }

    fun canStartTimer(todo: Todo): Boolean {
        return activeTimerManager.canStartTimer(todo.id)
    }

    fun startTimer(todo: Todo) {
        when {
            !activeTimerManager.canStartTimer(todo.id) -> {
                val activeId = activeTimerManager.getActiveTimerId()
                throw TimerException.AlreadyRunningException(activeId ?: -1)
            }
        }

        activeTimerManager.startTimer(todo.id)

        when (todo.status) {
            TodoStatus.PENDING -> updateTodoStatus(todo, TodoStatus.IN_PROGRESS)
            else -> {}
        }
    }

    fun pauseTimer(todo: Todo, elapsedSeconds: Int) {
        try {
            todoService.updateTimerSeconds(userId, todo.id, elapsedSeconds)
            activeTimerManager.stopTimer(todo.id)
        } catch (e: Exception) {
            throw TimerException.SaveFailedException(e)
        }
    }

    fun resetTimer(todo: Todo) {
        try {
            todoService.updateTimerSeconds(userId, todo.id, 0)
            activeTimerManager.stopTimer(todo.id)
        } catch (e: Exception) {
            throw TimerException.SaveFailedException(e)
        }
    }

    fun getActiveTimerId(): Long? {
        return activeTimerManager.getActiveTimerId()
    }

    private fun updateTodoStatus(todo: Todo, status: TodoStatus) {
        todo.status = status
        todoService.updateTodo(todo)
    }
}
