package com.lsk.learningtracker.user.model

import org.mindrot.jbcrypt.BCrypt

class Password private constructor(
    private val rawPassword: String
) {
    init {
        validateLength(rawPassword)
    }

    fun hash(): String {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt())
    }

    private fun validateLength(password: String) {
        require(password.length in MIN_LENGTH..MAX_LENGTH) {
            "비밀번호는 ${MIN_LENGTH}자 이상 ${MAX_LENGTH}자 이하여야 합니다."
        }
    }

    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 20

        operator fun invoke(rawPassword: String): Password {
            return Password(rawPassword)
        }

        fun matches(rawPassword: String, hashedPassword: String): Boolean {
            return BCrypt.checkpw(rawPassword, hashedPassword)
        }
    }
}
