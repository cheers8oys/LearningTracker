package com.lsk.learningtracker.user.service

import com.lsk.learningtracker.user.model.User
import com.lsk.learningtracker.user.repository.UserRepository

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

    fun login(username: String, password: String): User {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다.")

        require(user.matchesPassword(password)) {
            "비밀번호가 일치하지 않습니다."
        }

        return user
    }
}