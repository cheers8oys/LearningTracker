package com.lsk.learningtracker.user.repository

import com.lsk.learningtracker.user.model.User
import com.lsk.learningtracker.utils.DatabaseManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserRepository {

    fun save(user: User): User {

        if (findByUsername(user.username) != null) {
            throw IllegalArgumentException("이미 존재하는 사용자 이름입니다.")
        }

        val sql = """
            INSERT INTO users (username, password, created_at)
            VALUES (?, ?, ?)
        """

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, user.username)
                stmt.setString(2, user.getPasswordHash())
                stmt.setString(3, formatDateTime(LocalDateTime.now()))
                stmt.executeUpdate()
            }
        }

        return user
    }

    fun findByUsername(username: String): User? {
        val sql = "SELECT * FROM users WHERE username = ?"

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, username)
                val rs = stmt.executeQuery()

                return if (rs.next()) {
                    User(
                        username = rs.getString("username"),
                        passwordHash = rs.getString("password"),
                        autoLoginToken = rs.getString("auto_login_token"),
                        tokenExpiresAt = parseDateTime(rs.getString("token_expires_at"))
                    )
                } else {
                    null
                }
            }
        }
    }

    fun findByAutoLoginToken(token: String): User? {
        val sql = "SELECT * FROM users WHERE auto_login_token = ?"

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, token)
                val rs = stmt.executeQuery()

                return if (rs.next()) {
                    User.fromDatabase(
                        username = rs.getString("username"),
                        passwordHash = rs.getString("password"),
                        autoLoginToken = rs.getString("auto_login_token"),
                        tokenExpiresAt = parseDateTime(rs.getString("token_expires_at"))
                    )
                } else {
                    null
                }
            }
        }
    }

    fun updateAutoLoginToken(username: String, token: String?, expiresAt: LocalDateTime?) {
        val sql = """
            UPDATE users 
            SET auto_login_token = ?, token_expires_at = ?
            WHERE username = ?
        """

        DatabaseManager.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, token)
                stmt.setString(2, expiresAt?.let { formatDateTime(it) })
                stmt.setString(3, username)
                stmt.executeUpdate()
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
                    users.add(
                        User(
                            username = rs.getString("username"),
                            passwordHash = rs.getString("password")
                        )
                    )
                }
            }
        }

        return users
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