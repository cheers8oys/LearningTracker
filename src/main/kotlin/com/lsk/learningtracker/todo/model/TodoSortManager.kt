package com.lsk.learningtracker.todo.model

class TodoSortManager {

    fun sortByPriority(todos: List<Todo>): List<Todo> {
        return todos.sortedWith(
            compareBy<Todo> { it.priority.sortOrder }
                .thenBy { it.createdAt }
        )
    }

    fun sortByPriorityDescending(todos: List<Todo>): List<Todo> {
        return todos.sortedWith(
            compareByDescending<Todo> { it.priority.sortOrder }
                .thenBy { it.createdAt }
        )
    }

    fun sortByCreatedDate(todos: List<Todo>): List<Todo> {
        return todos.sortedBy { it.createdAt }
    }

    fun sortByCreatedDateDescending(todos: List<Todo>): List<Todo> {
        return todos.sortedByDescending { it.createdAt }
    }

    fun sortByTimerSeconds(todos: List<Todo>): List<Todo> {
        return todos.sortedByDescending { it.timerSeconds }
    }
}
