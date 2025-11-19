package com.lsk.learningtracker.todo.service

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.repository.TodoRepository
import java.time.LocalDateTime

class TodoService(
    private val todoRepository: TodoRepository
) {
    fun createTodo(userId: Long, content: String): Todo {
        val todo = Todo(
            userId = userId,
            content = content,
            status = com.lsk.learningtracker.todo.model.TodoStatus.PENDING,
            createdDate = java.time.LocalDate.now(),
            createdAt = LocalDateTime.now()
        )
        val id = todoRepository.save(todo)
        return todo.copy(id = id)
    }

    fun getTodayTodos(userId: Long): List<Todo> {
        return todoRepository.findTodayTodos(userId)
    }

    fun updateTodo(todo: Todo) {
        todoRepository.update(todo)
    }

    fun deleteTodo(todoId: Long) {
        todoRepository.delete(todoId)
    }
}