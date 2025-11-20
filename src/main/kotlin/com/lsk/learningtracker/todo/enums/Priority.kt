package com.lsk.learningtracker.todo.enums

enum class Priority(
    val displayName: String,
    val sortOrder: Int
) {
    HIGH("중요", 1),
    MEDIUM("보통", 2),
    LOW("낮음", 3);

    companion object {
        fun fromDisplayName(name: String): Priority {
            return values().find { it.displayName == name } ?: MEDIUM
        }
    }
}