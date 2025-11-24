package com.lsk.learningtracker.studyRecord.model

import java.time.LocalDate

data class StudyRecord(
    val id: Long = 0,
    val userId: Long,
    val studyDate: LocalDate,
    val totalStudySeconds: Int,
    val completedTodoCount: Int,
    val totalTodoCount: Int
)