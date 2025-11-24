package com.lsk.learningtracker.user.controller

import com.lsk.learningtracker.user.model.User

class AuthController(
    private val user: User,
    private val onLogout: () -> Unit
) {
    fun getCurrentUser(): User {
        return user
    }

    fun getUserId(): Long {
        return user.id
    }

    fun logout() {
        onLogout()
    }
}
