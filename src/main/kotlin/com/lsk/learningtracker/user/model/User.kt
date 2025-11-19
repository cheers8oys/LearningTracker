package com.lsk.learningtracker.user.model

import java.time.LocalDateTime

data class User(
    val username: String,
    private val passwordHash: String,
    var autoLoginToken: String? = null,
    var tokenExpiresAt: LocalDateTime? = null
) {
    init {
        validateUsername()
    }

    private fun validateUsername() {
        require(username.length in USERNAME_MIN..USERNAME_MAX) {
            "username은 ${USERNAME_MIN}-${USERNAME_MAX}자여야 합니다."
        }
        require(username.all { it.isLetterOrDigit() }) {
            "username은 영문과 숫자만 가능합니다."
        }
    }

    fun matchesPassword(rawPassword: String): Boolean {
        return Password.matches(rawPassword, passwordHash)
    }

    fun getPasswordHash(): String? {
        return passwordHash
    }

    fun generateAutoLoginToken(): AutoLoginToken {
        val token = AutoLoginToken()
        this.autoLoginToken = token.token
        this.tokenExpiresAt = token.expiresAt
        return token
    }

    fun clearAutoLoginToken() {
        this.autoLoginToken = null
        this.tokenExpiresAt = null
    }

    fun hasValidAutoLoginToken(): Boolean {
        if (autoLoginToken == null || tokenExpiresAt == null) {
            return false
        }
        return LocalDateTime.now().isBefore(tokenExpiresAt)
    }

    companion object {
        private const val USERNAME_MIN = 4
        private const val USERNAME_MAX = 20

        fun create(username: String, rawPassword: String): User {
            val password = Password(rawPassword)
            val passwordHash = password.hash()
            return User(username, passwordHash)
        }

        fun fromDatabase(
            username: String,
            passwordHash: String,
            autoLoginToken: String? = null,
            tokenExpiresAt: LocalDateTime? = null
        ): User {
            return User(username, passwordHash, autoLoginToken, tokenExpiresAt)
        }
    }
}