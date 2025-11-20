package com.lsk.learningtracker.todo.filter

import com.lsk.learningtracker.todo.enums.TodoFilter
import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.enums.TodoStatus

class TodoFilterManager {

    private var currentFilter: TodoFilter = TodoFilter.ALL

    fun setFilter(filter: TodoFilter) {
        currentFilter = filter
    }

    fun getCurrentFilter(): TodoFilter {
        return currentFilter
    }

    fun applyFilter(todos: List<Todo>): List<Todo> {
        return when (currentFilter) {
            TodoFilter.ALL -> todos
            TodoFilter.PENDING -> filterByStatus(todos, TodoStatus.PENDING)
            TodoFilter.IN_PROGRESS -> filterByStatus(todos, TodoStatus.IN_PROGRESS)
            TodoFilter.COMPLETED -> filterByStatus(todos, TodoStatus.COMPLETED)
        }
    }

    private fun filterByStatus(todos: List<Todo>, status: TodoStatus): List<Todo> {
        return todos.filter { it.status == status }
    }

    fun getFilterLabel(): String {
        return when (currentFilter) {
            TodoFilter.ALL -> "전체"
            TodoFilter.PENDING -> "대기"
            TodoFilter.IN_PROGRESS -> "진행중"
            TodoFilter.COMPLETED -> "완료"
        }
    }
}
