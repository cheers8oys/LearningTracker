package com.lsk.learningtracker.user

import com.lsk.learningtracker.user.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class UserTest {

    companion object {
        @JvmStatic
        fun invalidUsernameLengths() = listOf(
            "abc",
            "ab",
            "a",
            "",
            "a".repeat(21),
            "a".repeat(30)
        )

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

    @Test
    @DisplayName("올바른 정보로 User 생성 성공")
    fun createValidUser() {
        val user = User.create("pobi1234", "password123")
        assertEquals("pobi1234", user.username)
    }

    @ParameterizedTest
    @DisplayName("username 길이가 범위를 벗어나면 예외 발생")
    @MethodSource("invalidUsernameLengths")
    fun validateUsernameLength(username: String) {
        assertThrows<IllegalArgumentException> {
            User.create(username, "password123")
        }
    }

    @ParameterizedTest
    @DisplayName("username에 특수문자나 공백이 포함되면 예외 발생")
    @ValueSource(strings = ["pobi@123", "pobi 123", "pobi!123", "pobi#123"])
    fun validateUsernameFormat(username: String) {
        assertThrows<IllegalArgumentException> {
            User.create(username, "password123")
        }
    }

    @ParameterizedTest
    @DisplayName("비밀번호 길이가 범위를 벗어나면 예외 발생")
    @MethodSource("invalidPasswordLengths")
    fun validatePasswordLength(password: String) {
        assertThrows<IllegalArgumentException> {
            User.create("pobi1234", password)
        }
    }

    @Test
    @DisplayName("올바른 비밀번호로 로그인 성공")
    fun matchesPasswordSuccess() {
        val user = User.create("pobi1234", "password123")

        assertTrue(user.matchesPassword("password123"))
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    fun matchesPasswordFailure() {
        val user = User.create("pobi1234", "password123")

        assertFalse(user.matchesPassword("wrongpass"))
    }

    @ParameterizedTest
    @DisplayName("다양한 올바른 username으로 User 생성 성공")
    @ValueSource(strings = ["pobi1234", "test0000", "user9999", "abcd1234"])
    fun createUserWithValidUsernames(username: String) {
        val user = User.create(username, "password123")
        assertEquals(username, user.username)
    }
}
