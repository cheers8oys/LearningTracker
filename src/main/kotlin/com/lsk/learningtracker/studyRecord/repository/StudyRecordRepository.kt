package com.lsk.learningtracker.studyRecord.repository

import com.lsk.learningtracker.studyRecord.model.StudyRecord
import com.lsk.learningtracker.utils.DatabaseManager
import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudyRecordRepository {

    fun save(record: StudyRecord) {
        val sql = """
            INSERT OR REPLACE INTO study_records 
            (user_id, study_date, total_study_seconds, completed_todo_count, total_todo_count)
            VALUES (?, ?, ?, ?, ?)
        """

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, record.userId)
                stmt.setString(2, formatDate(record.studyDate))
                stmt.setInt(3, record.totalStudySeconds)
                stmt.setInt(4, record.completedTodoCount)
                stmt.setInt(5, record.totalTodoCount)
                stmt.executeUpdate()
            }
        }
    }

    fun findByUserAndDate(userId: Long, date: LocalDate): StudyRecord? {
        val sql = "SELECT * FROM study_records WHERE user_id = ? AND study_date = ?"

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, userId)
                stmt.setString(2, formatDate(date))
                val rs = stmt.executeQuery()
                return when {
                    rs.next() -> mapToStudyRecord(rs)
                    else -> null
                }
            }
        }
    }

    fun findByUserAndDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): List<StudyRecord> {
        val sql = """
            SELECT * FROM study_records 
            WHERE user_id = ? AND study_date BETWEEN ? AND ?
            ORDER BY study_date ASC
        """

        val records = mutableListOf<StudyRecord>()
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, userId)
                stmt.setString(2, formatDate(startDate))
                stmt.setString(3, formatDate(endDate))
                val rs = stmt.executeQuery()

                while (rs.next()) {
                    records.add(mapToStudyRecord(rs))
                }
            }
        }
        return records
    }

    private fun mapToStudyRecord(rs: ResultSet): StudyRecord {
        return StudyRecord(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            studyDate = parseDate(rs.getString("study_date")),
            totalStudySeconds = rs.getInt("total_study_seconds"),
            completedTodoCount = rs.getInt("completed_todo_count"),
            totalTodoCount = rs.getInt("total_todo_count")
        )
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
