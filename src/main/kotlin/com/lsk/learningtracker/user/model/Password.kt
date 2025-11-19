package com.lsk.learningtracker.user.model

import org.mindrot.jbcrypt.BCrypt

data class Password(private val value: String) {

    init {
        validate(value)
    }

    private fun validate(password: String) {
        require(password.length in MIN_LENGTH..MAX_LENGTH) {
            "비밀번호는 ${MIN_LENGTH}-${MAX_LENGTH}자여야 합니다."
        }
    }

    fun hash(): String {
        return BCrypt.hashpw(value, BCrypt.gensalt(WORK_FACTOR))
    }

    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 20
        private const val WORK_FACTOR = 12

        fun matches(rawPassword: String, hashedPassword: String): Boolean {
            return BCrypt.checkpw(rawPassword, hashedPassword)
        }
    }
}
