package com.lsk.learningtracker.utils

import java.sql.Connection
import java.sql.DriverManager

object DatabaseManager {
    private const val DB_URL = "jdbc:sqlite:studytracker.db"
    private var connection: Connection? = null

    fun getConnection(): Connection {
        if (connection == null || connection?.isClosed == true) {
            connection = DriverManager.getConnection(DB_URL)
        }
        return connection!!
    }

    fun initializeDatabase() {
        val schemaSQL = loadSchemaSQL()
        executeSQL(schemaSQL)
    }

    private fun loadSchemaSQL(): String {
        return this::class.java.classLoader
            .getResourceAsStream("db/schema.sql")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("schema.sql 파일을 찾을 수 없습니다.")
    }

    private fun executeSQL(sql: String) {
        val statements = sql.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                for (statement in statements) {
                    stmt.execute(statement)
                }
            }
        }
    }

    fun closeConnection() {
        connection?.close()
        connection = null
    }

    // 테스트용: 모든 데이터 삭제
    fun clearDatabase() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("DELETE FROM users")
                stmt.execute("DELETE FROM todos")
            }
        }
    }

    // 테이블 재생성 (스키마 변경 시)
    fun recreateDatabase() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("DROP TABLE IF EXISTS users")
                stmt.execute("DROP TABLE IF EXISTS todos")
            }
        }
        initializeDatabase()
    }
}