package com.lsk.learningtracker.todo

import com.lsk.learningtracker.todo.model.TodoStatus
import com.lsk.learningtracker.todo.repository.TodoRepository
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.utils.DatabaseManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TodoServiceTest {

    private lateinit var todoRepository: TodoRepository
    private lateinit var todoService: TodoService
    private val testUserId = 1L

    @BeforeEach
    fun setUp() {
        DatabaseManager.recreateDatabase()
        todoRepository = TodoRepository()
        todoService = TodoService(todoRepository)
    }

    @Test
    fun 투두_생성_테스트() {
        val content = "테스트 투두 아이템"
        val todo = todoService.createTodo(testUserId, content)

        assertEquals(testUserId, todo.userId)
        assertEquals(content, todo.content)
        assertEquals(TodoStatus.PENDING, todo.status)
        assertTrue(todo.id > 0)
    }

    @Test
    fun 오늘_날짜_투두_조회_테스트() {
        val content1 = "첫번째 투두"
        val content2 = "두번째 투두"

        todoService.createTodo(testUserId, content1)
        todoService.createTodo(testUserId, content2)

        val todos = todoService.getTodayTodos(testUserId)
        assertEquals(2, todos.size)
        assertEquals(content1, todos[0].content)
        assertEquals(content2, todos[1].content)
    }
}