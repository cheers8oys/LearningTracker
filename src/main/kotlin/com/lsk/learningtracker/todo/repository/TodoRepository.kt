package com.lsk.learningtracker.todo.repository

import com.lsk.learningtracker.todo.model.Todo
import com.lsk.learningtracker.todo.enums.TodoStatus
import com.lsk.learningtracker.utils.DatabaseManager
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TodoRepository {

    fun save(todo: Todo): Long {
        val sql = """
            INSERT INTO todos (user_id, content, status, created_date, created_at)
            VALUES (?, ?, ?, ?, ?)
        """
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, todo.userId)
                stmt.setString(2, todo.content)
                stmt.setString(3, todo.status.name)
                stmt.setString(4, formatDate(todo.createdDate))
                stmt.setString(5, formatDateTime(todo.createdAt))
                stmt.executeUpdate()
            }
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT last_insert_rowid()")
                if (rs.next()) {
                    return rs.getLong(1)
                }
            }
        }
        throw IllegalStateException("Todo 저장에 실패했습니다.")
    }

    fun findTodayTodos(userId: Long): List<Todo> {
        val today = formatDate(LocalDate.now())
        val sql = """
            SELECT * FROM todos 
            WHERE user_id = ? AND created_date = ?
            ORDER BY created_at ASC
        """

        val todos = mutableListOf<Todo>()
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, userId)
                stmt.setString(2, today)
                val rs = stmt.executeQuery()

                while (rs.next()) {
                    todos.add(mapToTodo(rs))
                }
            }
        }
        return todos
    }

    fun update(todo: Todo) {
        val sql = """
            UPDATE todos 
            SET content = ?, status = ?, timer_seconds = ?, completed_at = ?
            WHERE id = ?
        """
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, todo.content)
                stmt.setString(2, todo.status.name)
                stmt.setInt(3, todo.timerSeconds)
                stmt.setString(4, todo.completedAt?.let { formatDateTime(it) })
                stmt.setLong(5, todo.id)
                stmt.executeUpdate()
            }
        }
    }

    fun delete(id: Long) {
        val sql = "DELETE FROM todos WHERE id = ?"
        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeUpdate()
            }
        }
    }

    private fun mapToTodo(rs: ResultSet): Todo {
        return Todo(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            content = rs.getString("content"),
            status = TodoStatus.valueOf(rs.getString("status")),
            timerSeconds = rs.getInt("timer_seconds"),
            completedAt = parseDateTime(rs.getString("completed_at")),
            createdDate = parseDate(rs.getString("created_date")),
            createdAt = parseDateTime(rs.getString("created_at"))!!
        )
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun parseDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    }
}