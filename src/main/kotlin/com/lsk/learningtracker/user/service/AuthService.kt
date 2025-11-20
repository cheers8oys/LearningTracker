package com.lsk.learningtracker.user.service

import com.lsk.learningtracker.user.model.User
import com.lsk.learningtracker.user.repository.UserRepository
import java.time.LocalDateTime
import java.util.*

class AuthService(
    private val userRepository: UserRepository
) {
    fun register(username: String, password: String, confirmPassword: String): User {
        validatePasswordsMatch(password, confirmPassword)
        validateUsernameNotExists(username)

        val user = User.create(username, password)
        userRepository.save(user)
        return user
    }

    fun login(username: String, password: String, rememberMe: Boolean): User {
        val user = findUserByUsername(username)
        validatePassword(password, user)

        return when {
            rememberMe -> issueAutoLoginToken(user)
            else -> user
        }
    }

    fun autoLogin(): User? {
        val users = userRepository.findAll()
        val validUser = users.firstOrNull { isAutoLoginTokenValid(it) }
        return validUser
    }

    fun logout(user: User) {
        val updatedUser = user.clearAutoLoginToken()
        userRepository.update(updatedUser)
    }

    private fun validatePasswordsMatch(password: String, confirmPassword: String) {
        require(password == confirmPassword) {
            "비밀번호가 일치하지 않습니다."
        }
    }

    private fun validateUsernameNotExists(username: String) {
        val existingUser = userRepository.findByUsername(username)
        require(existingUser == null) {
            "이미 사용 중인 사용자명입니다."
        }
    }

    private fun findUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
    }

    private fun validatePassword(rawPassword: String, user: User) {
        require(user.matchesPassword(rawPassword)) {
            "비밀번호가 일치하지 않습니다."
        }
    }

    private fun issueAutoLoginToken(user: User): User {
        val token = generateToken()
        val expiresAt = LocalDateTime.now().plusDays(TOKEN_VALIDITY_DAYS)
        val updatedUser = user.updateAutoLoginToken(token, expiresAt)
        userRepository.update(updatedUser)
        return updatedUser
    }

    private fun isAutoLoginTokenValid(user: User): Boolean {
        val token = user.autoLoginToken ?: return false
        val expiresAt = user.tokenExpiresAt ?: return false
        return expiresAt.isAfter(LocalDateTime.now())
    }

    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }

    companion object {
        private const val TOKEN_VALIDITY_DAYS = 30L
    }
}
