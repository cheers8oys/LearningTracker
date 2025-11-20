package com.lsk.learningtracker.user.model

import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    val username: String,
    val password: String,
    val autoLoginToken: String? = null,
    val tokenExpiresAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun matchesPassword(rawPassword: String): Boolean {
        return Password.matches(rawPassword, this.password)
    }

    fun updateAutoLoginToken(token: String, expiresAt: LocalDateTime): User {
        return this.copy(
            autoLoginToken = token,
            tokenExpiresAt = expiresAt
        )
    }

    fun clearAutoLoginToken(): User {
        return this.copy(
            autoLoginToken = null,
            tokenExpiresAt = null
        )
    }

    companion object {
        private const val MIN_USERNAME_LENGTH = 4
        private const val MAX_USERNAME_LENGTH = 20
        private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9]+$")

        fun create(username: String, rawPassword: String): User {
            validateUsername(username)
            val hashedPassword = Password(rawPassword).hash()
            return User(
                username = username,
                password = hashedPassword
            )
        }

        private fun validateUsername(username: String) {
            require(username.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH) {
                "사용자명은 ${MIN_USERNAME_LENGTH}자 이상 ${MAX_USERNAME_LENGTH}자 이하여야 합니다."
            }
            require(USERNAME_PATTERN.matches(username)) {
                "사용자명은 영문자와 숫자만 포함할 수 있습니다."
            }
        }
    }
}
