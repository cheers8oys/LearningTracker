package com.lsk.learningtracker.studyRecord.service

import com.lsk.learningtracker.study.service.StudyRecordService
import com.lsk.learningtracker.studyRecord.model.StatisticsData
import java.time.DayOfWeek
import java.time.LocalDate

class StatisticsService(
    private val studyRecordService: StudyRecordService
) {
    fun getDailyStatistics(userId: Long, date: LocalDate): StatisticsData {
        val record = studyRecordService.getOrCalculateStudyRecord(userId, date)

        return when (record) {
            null -> StatisticsData(0, 0, 0, 0, 0)
            else -> StatisticsData(
                totalStudySeconds = record.totalStudySeconds,
                totalTodoCount = record.totalTodoCount,
                completedTodoCount = record.completedTodoCount,
                averageStudySeconds = record.totalStudySeconds,
                studyDayCount = if (record.totalTodoCount > 0) 1 else 0
            )
        }
    }

    fun getWeeklyStatistics(userId: Long, date: LocalDate): StatisticsData {
        val startOfWeek = date.with(DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(6)

        return calculateRangeStatistics(userId, startOfWeek, endOfWeek)
    }

    fun getMonthlyStatistics(userId: Long, date: LocalDate): StatisticsData {
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = date.withDayOfMonth(date.lengthOfMonth())

        return calculateRangeStatistics(userId, startOfMonth, endOfMonth)
    }

    private fun calculateRangeStatistics(userId: Long, startDate: LocalDate, endDate: LocalDate): StatisticsData {
        val records = studyRecordService.getOrCalculateStudyRecords(userId, startDate, endDate)

        val totalStudySeconds = records.values.sumOf { it.totalStudySeconds }
        val totalTodoCount = records.values.sumOf { it.totalTodoCount }
        val completedTodoCount = records.values.sumOf { it.completedTodoCount }
        val studyDayCount = records.values.count { it.totalTodoCount > 0 }

        val averageStudySeconds = when {
            studyDayCount == 0 -> 0
            else -> totalStudySeconds / studyDayCount
        }

        return StatisticsData(
            totalStudySeconds = totalStudySeconds,
            totalTodoCount = totalTodoCount,
            completedTodoCount = completedTodoCount,
            averageStudySeconds = averageStudySeconds,
            studyDayCount = studyDayCount
        )
    }
}