package com.lsk.learningtracker.todo.service

import com.lsk.learningtracker.todo.enums.Priority
import com.lsk.learningtracker.todo.enums.TodoStatus
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.repository.TodoRepository
import java.time.LocalDate
import java.time.LocalDateTime

class TodoService(
    private val todoRepository: TodoRepository
) {
    fun createTodo(userId: Long, content: String, priority: Priority = Priority.MEDIUM): Todo {
        val todo = Todo(
            userId = userId,
            content = content,
            status = TodoStatus.PENDING,
            priority = priority,
            createdDate = LocalDate.now(),
            createdAt = LocalDateTime.now()
        )
        val id = todoRepository.save(todo)
        return todo.copy(id = id)
    }

    fun getTodayTodos(userId: Long): List<Todo> {
        return todoRepository.findTodayTodos(userId)
    }

    fun getTodosByDate(userId: Long, date: LocalDate): List<Todo> {
        return todoRepository.findTodosByDate(userId, date)
    }

    fun updateTodo(todo: Todo) {
        todoRepository.update(todo)
    }

    fun deleteTodo(todoId: Long) {
        todoRepository.delete(todoId)
    }

    fun updateTimerSeconds(userId: Long, todoId: Long, seconds: Int) {
        val todos = getTodayTodos(userId)
        val todo = todos.find { it.id == todoId } ?: throw IllegalArgumentException("해당 투두 없음")
        todo.timerSeconds = seconds
        updateTodo(todo)
    }
}
