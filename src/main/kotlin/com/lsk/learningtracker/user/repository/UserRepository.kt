package com.lsk.learningtracker.user.repository

import com.lsk.learningtracker.user.model.User
import com.lsk.learningtracker.utils.DatabaseManager
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserRepository {

    fun save(user: User) {
        val sql = """
            INSERT INTO users (username, password, created_at)
            VALUES (?, ?, ?)
        """

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, user.username)
                stmt.setString(2, user.password)
                stmt.setString(3, formatDateTime(user.createdAt))
                stmt.executeUpdate()
            }
        }
    }

    fun findByUsername(username: String): User? {
        val sql = "SELECT * FROM users WHERE username = ?"

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, username)
                val rs = stmt.executeQuery()
                return when {
                    rs.next() -> mapToUser(rs)
                    else -> null
                }
            }
        }
    }

    fun findAll(): List<User> {
        val sql = "SELECT * FROM users"
        val users = mutableListOf<User>()

        DatabaseManager.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(sql)
                while (rs.next()) {
                    users.add(mapToUser(rs))
                }
            }
        }
        return users
    }

    fun update(user: User) {
        val sql = """
            UPDATE users 
            SET auto_login_token = ?, token_expires_at = ?
            WHERE username = ?
        """

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, user.autoLoginToken)
                stmt.setString(2, user.tokenExpiresAt?.let { formatDateTime(it) })
                stmt.setString(3, user.username)
                stmt.executeUpdate()
            }
        }
    }

    private fun mapToUser(rs: ResultSet): User {
        return User(
            id = rs.getLong("id"),
            username = rs.getString("username"),
            password = rs.getString("password"),
            autoLoginToken = rs.getString("auto_login_token"),
            tokenExpiresAt = parseDateTime(rs.getString("token_expires_at")),
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
}
