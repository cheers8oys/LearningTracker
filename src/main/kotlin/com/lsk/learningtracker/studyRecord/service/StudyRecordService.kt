package com.lsk.learningtracker.study.service

import com.lsk.learningtracker.studyRecord.model.StudyRecord
import com.lsk.learningtracker.studyRecord.repository.StudyRecordRepository
import com.lsk.learningtracker.todo.enums.TodoStatus
import com.lsk.learningtracker.todo.service.TodoService
import java.time.LocalDate

class StudyRecordService(
    private val studyRecordRepository: StudyRecordRepository,
    private val todoService: TodoService
) {
    fun updateStudyRecord(userId: Long, date: LocalDate) {
        val todos = todoService.getTodosByDate(userId, date)

        val totalStudySeconds = todos.sumOf { it.timerSeconds }
        val completedCount = todos.count { it.status == TodoStatus.COMPLETED }
        val totalCount = todos.size

        val record = StudyRecord(
            userId = userId,
            studyDate = date,
            totalStudySeconds = totalStudySeconds,
            completedTodoCount = completedCount,
            totalTodoCount = totalCount
        )

        studyRecordRepository.save(record)
    }

    fun getStudyRecord(userId: Long, date: LocalDate): StudyRecord? {
        return studyRecordRepository.findByUserAndDate(userId, date)
    }

    fun getStudyRecords(userId: Long, startDate: LocalDate, endDate: LocalDate): List<StudyRecord> {
        return studyRecordRepository.findByUserAndDateRange(userId, startDate, endDate)
    }

    fun getOrCalculateStudyRecord(userId: Long, date: LocalDate): StudyRecord? {
        val existingRecord = getStudyRecord(userId, date)
        if (existingRecord != null) {
            return existingRecord
        }

        val todos = todoService.getTodosByDate(userId, date)
        if (todos.isEmpty()) {
            return null
        }

        val totalStudySeconds = todos.sumOf { it.timerSeconds }
        val completedCount = todos.count { it.status == TodoStatus.COMPLETED }
        val totalCount = todos.size

        return StudyRecord(
            userId = userId,
            studyDate = date,
            totalStudySeconds = totalStudySeconds,
            completedTodoCount = completedCount,
            totalTodoCount = totalCount
        )
    }

    fun getOrCalculateStudyRecords(userId: Long, startDate: LocalDate, endDate: LocalDate): Map<LocalDate, StudyRecord> {
        val records = mutableMapOf<LocalDate, StudyRecord>()

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val record = getOrCalculateStudyRecord(userId, currentDate)
            if (record != null) {
                records[currentDate] = record
            }
            currentDate = currentDate.plusDays(1)
        }

        return records
    }

    fun hasStudyOnDate(userId: Long, date: LocalDate): Boolean {
        val record = getStudyRecord(userId, date)
        return record != null && record.totalTodoCount > 0
    }
}
