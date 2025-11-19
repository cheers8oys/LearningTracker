package com.lsk.learningtracker.user.model

import java.time.LocalDateTime
import java.util.*

data class AutoLoginToken(
    val token: String = UUID.randomUUID().toString(),
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(TOKEN_VALIDITY_DAYS)
) {
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    companion object {
        private const val TOKEN_VALIDITY_DAYS = 30L
    }
}