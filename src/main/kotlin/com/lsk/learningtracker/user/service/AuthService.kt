package com.lsk.learningtracker.user.service

import com.lsk.learningtracker.user.model.User
import com.lsk.learningtracker.user.repository.UserRepository
import java.io.File

class AuthService(
    private val userRepository: UserRepository
) {

    fun register(username: String, password: String, confirmPassword: String): User {

        require(password == confirmPassword) {
            "비밀번호가 일치하지 않습니다."
        }
        val user = User(username, password)
        return userRepository.save(user)
    }

    fun login(username: String, password: String, rememberMe: Boolean): User {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다")

        require(user.matchesPassword(password)) {
            "아이디 또는 비밀번호가 일치하지 않습니다"
        }

        if (rememberMe) {
            val token = user.generateAutoLoginToken()
            userRepository.updateAutoLoginToken(user.username, token.token, token.expiresAt)
            saveTokenToLocal(token.token)
            println("✅ 자동로그인 토큰 저장: ${token.token}")
        }

        return user
    }

    fun autoLogin(): User? {
        val token = loadTokenFromLocal() ?: return null

        val user = userRepository.findByAutoLoginToken(token) ?: run {
            deleteTokenFromLocal()
            return null
        }

        if (!user.hasValidAutoLoginToken()) {
            deleteTokenFromLocal()
            user.clearAutoLoginToken()
            userRepository.updateAutoLoginToken(user.username, null, null)
            return null
        }

        println("✅ 자동로그인 성공: ${user.username}")
        return user
    }

    fun logout(user: User) {
        user.clearAutoLoginToken()
        userRepository.updateAutoLoginToken(user.username, null, null)
        deleteTokenFromLocal()
        println("✅ 로그아웃: ${user.username}")
    }

    private fun saveTokenToLocal(token: String) {
        File(TOKEN_FILE_PATH).writeText(token)
    }

    private fun loadTokenFromLocal(): String? {
        val file = File(TOKEN_FILE_PATH)
        return if (file.exists()) file.readText().trim() else null
    }

    private fun deleteTokenFromLocal() {
        File(TOKEN_FILE_PATH).delete()
    }

    companion object {
        private const val TOKEN_FILE_PATH = "auto_login.token"
    }

}