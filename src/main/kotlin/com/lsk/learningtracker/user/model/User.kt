package com.lsk.learningtracker.user.model

data class User(
    val username: String,
    val password: String
) {
    init {
        validateUsername()
        validatePassword()
    }

    private fun validateUsername() {
        require(username.length in 4..20) {
            "username은 4-20자여야 합니다."
        }
        require(username.all { it.isLetterOrDigit() }) {
            "username은 영문과 숫자만 가능합니다."
        }
    }

    private fun validatePassword() {
        require(password.length in 8..20) {
            "비밀번호는 8-20자여야 합니다."
        }
    }
}