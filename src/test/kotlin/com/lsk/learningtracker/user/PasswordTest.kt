package com.lsk.learningtracker.user

import com.lsk.learningtracker.user.model.Password
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PasswordTest {

    @Test
    @DisplayName("올바른 비밀번호로 Password 생성 성공")
    fun createValidPassword() {
        val password = Password("password123")
        assertNotNull(password)
    }

    @ParameterizedTest
    @DisplayName("비밀번호 길이가 범위를 벗어나면 예외 발생")
    @MethodSource("invalidPasswordLengths")
    fun validatePasswordLength(rawPassword: String) {
        assertThrows<IllegalArgumentException> {
            Password(rawPassword)
        }
    }

    @Test
    @DisplayName("비밀번호를 BCrypt로 해싱한다")
    fun hashPassword() {
        val password = Password("password123")

        val hashed = password.hash()

        assertNotEquals("password123", hashed) // 평문이 아님
        assertTrue(hashed.startsWith("\$2a\$")) // BCrypt 형식
    }

    @Test
    @DisplayName("같은 비밀번호를 여러 번 해싱하면 다른 결과")
    fun hashPasswordMultipleTimes() {
        val password1 = Password("password123")
        val password2 = Password("password123")

        val hash1 = password1.hash()
        val hash2 = password2.hash()

        assertNotEquals(hash1, hash2)
    }

    @Test
    @DisplayName("원본 비밀번호와 해시된 비밀번호를 검증한다")
    fun verifyPassword() {

        val password = Password("password123")
        val hashed = password.hash()

        assertTrue(Password.matches("password123", hashed))
        assertFalse(Password.matches("wrongpass", hashed))
    }

    companion object {
        @JvmStatic
        fun invalidPasswordLengths() = listOf(
            "1234567",
            "123456",
            "abc",
            "",
            "a".repeat(21),
            "a".repeat(30)
        )
    }
}