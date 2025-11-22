package com.lsk.learningtracker.studyRecord.model

data class StatisticsData(
    val totalStudySeconds: Int,
    val totalTodoCount: Int,
    val completedTodoCount: Int,
    val averageStudySeconds: Int,
    val studyDayCount: Int
) {
    val completionRate: Double
        get() = when {
            totalTodoCount == 0 -> 0.0
            else -> (completedTodoCount.toDouble() / totalTodoCount * 100)
        }
}