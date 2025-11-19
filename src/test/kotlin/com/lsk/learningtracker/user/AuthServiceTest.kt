package com.lsk.learningtracker.user

import com.lsk.learningtracker.user.repository.UserRepository
import com.lsk.learningtracker.user.service.AuthService
import com.lsk.learningtracker.utils.DatabaseManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var authService: AuthService

    @BeforeEach
    fun settingUp() {
        userRepository = UserRepository()
        authService = AuthService(userRepository)
    }

    @ParameterizedTest
    @DisplayName("다양한 username과 password로 회원가입 성공")
    @CsvSource(
        "pobi1234, password123",
        "test0000, testpass456",
        "user9999, userpass789",
        "abcd1234, abcdpass000"
    )
    fun register(username: String, password: String) {
        val user = authService.register(username, password, password)

        assertNotNull(user)
        assertEquals(username, user.username)
    }

    @Test
    @DisplayName("중복된 username으로 회원가입 시도 시 예외 발생")
    fun registerDuplicateUsername() {
        assertThrows<IllegalArgumentException> {
            authService.register("pobi1234", "password456", "password456")
        }
    }

    @Test
    @DisplayName("로그인 성공")
    fun loginSuccess() {
        val user = authService.login("pobi1234", "password123", false)
        assertEquals("pobi1234", user.username)
    }

    @ParameterizedTest
    @DisplayName("로그인 실패 케이스")
    @CsvSource(
        "unknown, password123",
        "pobi1234, wrongpass"
    )
    fun loginFailure(username: String, password: String) {
        assertThrows<IllegalArgumentException> {
            authService.login(username, password, false)
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            DatabaseManager.clearDatabase()
            DatabaseManager.initializeDatabase()
        }
    }
}