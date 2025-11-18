package com.lsk.learningtracker.user

import com.lsk.learningtracker.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

        val user = User("pobi1234", "password123")
        assertEquals("pobi1234", user.username)
    }

    @ParameterizedTest
    @DisplayName("username 길이가 범위를 벗어나면 예외 발생")
    @MethodSource("invalidUsernameLengths")
    fun validateUsernameLength(username: String) {

        val exception = assertThrows<IllegalArgumentException> {
            User(username, "password123")
        }
        assertTrue(exception.message!!.contains("4-20자"))
    }

    @ParameterizedTest
    @DisplayName("username에 특수문자나 공백이 포함되면 예외 발생")
    @ValueSource(strings = ["pobi@123", "pobi 123", "pobi!123", "pobi#123"])
    fun validateUsernameFormat(username: String) {

        val exception = assertThrows<IllegalArgumentException> {
            User(username, "password123")
        }
        assertTrue(exception.message!!.contains("영문과 숫자"))
    }

    @ParameterizedTest
    @DisplayName("비밀번호 길이가 범위를 벗어나면 예외 발생")
    @MethodSource("invalidPasswordLengths")
    fun validatePasswordLength(password: String) {

        val exception = assertThrows<IllegalArgumentException> {
            User("pobi1234", password)
        }
        assertTrue(exception.message!!.contains("8-20자"))
    }

    @ParameterizedTest
    @DisplayName("다양한 올바른 username으로 User 생성 성공")
    @ValueSource(strings = ["pobi1234", "test0000", "user9999", "abcd1234"])
    fun createUserWithValidUsernames(username: String) { // when

        val user = User(username, "password123")
        assertEquals(username, user.username)
    }
}