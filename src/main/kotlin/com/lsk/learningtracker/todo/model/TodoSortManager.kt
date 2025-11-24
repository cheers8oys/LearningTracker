package com.lsk.learningtracker.todo.model

import com.lsk.learningtracker.todo.enums.TodoStatus

class TodoSortManager {

    fun sortByPriority(todos: List<Todo>): List<Todo> {
        return todos.sortedWith(
            compareBy<Todo> { getStatusSortOrder(it.status) }
                .thenBy { it.priority.sortOrder }
                .thenBy { it.createdAt }
        )
    }

    fun sortByPriorityDescending(todos: List<Todo>): List<Todo> {
        return todos.sortedWith(
            compareBy<Todo> { getStatusSortOrder(it.status) }
                .thenByDescending { it.priority.sortOrder }
                .thenBy { it.createdAt }
        )
    }

    fun sortByCreatedDate(todos: List<Todo>): List<Todo> {
        return todos.sortedWith(
            compareBy<Todo> { getStatusSortOrder(it.status) }
                .thenBy { it.createdAt }
        )
    }

    fun sortByCreatedDateDescending(todos: List<Todo>): List<Todo> {
        return todos.sortedWith(
            compareBy<Todo> { getStatusSortOrder(it.status) }
                .thenByDescending { it.createdAt }
        )
    }

    fun sortByTimerSeconds(todos: List<Todo>): List<Todo> {
        return todos.sortedWith(
            compareBy<Todo> { getStatusSortOrder(it.status) }
                .thenByDescending { it.timerSeconds }
        )
    }

    private fun getStatusSortOrder(status: TodoStatus): Int {
        return when (status) {
            TodoStatus.COMPLETED -> 0
            TodoStatus.IN_PROGRESS -> 1
            TodoStatus.PENDING -> 2
        }
    }
}

