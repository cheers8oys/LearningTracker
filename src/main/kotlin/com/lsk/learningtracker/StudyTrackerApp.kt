package com.lsk.learningtracker

import com.lsk.learningtracker.study.service.StudyRecordService
import com.lsk.learningtracker.studyRecord.repository.StudyRecordRepository
import com.lsk.learningtracker.studyRecord.service.StatisticsService
import com.lsk.learningtracker.todo.repository.TodoRepository
import com.lsk.learningtracker.todo.service.TodoService
import com.lsk.learningtracker.user.model.User
import com.lsk.learningtracker.user.repository.UserRepository
import com.lsk.learningtracker.user.service.AuthService
import com.lsk.learningtracker.user.view.LoginView
import com.lsk.learningtracker.user.view.MainView
import com.lsk.learningtracker.utils.DatabaseManager
import javafx.application.Application
import javafx.stage.Stage

class StudyTrackerApp : Application() {

    private lateinit var stage: Stage
    private lateinit var authService: AuthService
    private lateinit var todoService: TodoService
    private lateinit var studyRecordService: StudyRecordService
    private lateinit var statisticsService: StatisticsService

    override fun start(stage: Stage) {
        this.stage = stage
        DatabaseManager.initializeDatabase()

        val userRepository = UserRepository()
        authService = AuthService(userRepository)

        val todoRepository = TodoRepository()
        todoService = TodoService(todoRepository)

        val studyRecordRepository = StudyRecordRepository()
        studyRecordService = StudyRecordService(studyRecordRepository, todoService)

        statisticsService = StatisticsService(studyRecordService)

        val autoLoginUser = authService.autoLogin()
        when {
            autoLoginUser != null -> showMainView(autoLoginUser)
            else -> showLoginView()
        }
    }

    private fun showLoginView() {
        val loginView = LoginView(stage, authService) { user ->
            showMainView(user)
        }
        loginView.show()
    }

    private fun showMainView(user: User) {
        val mainView = MainView(stage, user, todoService, studyRecordService, statisticsService) {
            authService.logout(user)
            showLoginView()
        }
        mainView.show()
    }


    override fun stop() {
        DatabaseManager.closeConnection()
        println("✅ 앱 종료")
    }
}

fun main() {
    Application.launch(StudyTrackerApp::class.java)
}
