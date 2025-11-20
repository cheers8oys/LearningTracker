package com.lsk.learningtracker.todo.controller

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.model.TodoStatus
import com.lsk.learningtracker.todo.service.TodoService
import java.time.LocalDateTime

class TodoController(
    private val userId: Long,
    private val todoService: TodoService
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
        todoService.deleteTodo(todo.id)
    }

    fun completeTodo(todo: Todo) {
        todo.status = TodoStatus.COMPLETED
        todo.completedAt = LocalDateTime.now()
        todoService.updateTodo(todo)
    }

    fun startTimer(todo: Todo) {
        when (todo.status) {
            TodoStatus.PENDING -> updateTodoStatus(todo, TodoStatus.IN_PROGRESS)
            else -> {}
        }
    }

    fun pauseTimer(todo: Todo, elapsedSeconds: Int) {
        todoService.updateTimerSeconds(userId, todo.id, elapsedSeconds)
    }

    fun resetTimer(todo: Todo) {
        todoService.updateTimerSeconds(userId, todo.id, 0)
    }

    private fun updateTodoStatus(todo: Todo, status: TodoStatus) {
        todo.status = status
        todoService.updateTodo(todo)
    }
}
