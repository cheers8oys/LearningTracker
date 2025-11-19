package com.lsk.learningtracker.user.model

data class User(
    val username: String,
    private val passwordHash: String
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

    companion object {
        private const val USERNAME_MIN = 4
        private const val USERNAME_MAX = 20

        fun create(username: String, rawPassword: String): User {
            val password = Password(rawPassword)
            val passwordHash = password.hash()
            return User(username, passwordHash)
        }
    }
}