package com.lsk.learningtracker.todo.controller

import com.lsk.learningtracker.todo.enums.Priority
import com.lsk.learningtracker.todo.enums.TodoFilter
import com.lsk.learningtracker.todo.enums.TodoStatus
import com.lsk.learningtracker.todo.filter.TodoFilterManager
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.model.TodoSortManager
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.todo.timer.ActiveTimerManager
import com.lsk.learningtracker.todo.timer.TimerException
import java.time.LocalDateTime

class TodoController(
    private val userId: Long,
    private val todoService: TodoService,
    private val activeTimerManager: ActiveTimerManager,
    private val filterManager: TodoFilterManager,
    private val sortManager: TodoSortManager
) {
    fun getTodayTodos(): List<Todo> {
        val allTodos = todoService.getTodayTodos(userId)
        val filteredTodos = filterManager.applyFilter(allTodos)
        return sortManager.sortByPriority(filteredTodos)
    }

    fun setFilter(filter: TodoFilter) {
        filterManager.setFilter(filter)
    }

    fun getCurrentFilter(): TodoFilter {
        return filterManager.getCurrentFilter()
    }

    fun createTodo(content: String, priority: Priority = Priority.MEDIUM): Todo {
        return todoService.createTodo(userId, content, priority)
    }

    fun updateTodoContent(todo: Todo, newContent: String) {
        todo.content = newContent.trim()
        todoService.updateTodo(todo)
    }

    fun updateTodoPriority(todo: Todo, priority: Priority) {
        todo.priority = priority
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
