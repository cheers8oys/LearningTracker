package com.lsk.learningtracker

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

    override fun start(stage: Stage) {
        this.stage = stage
        DatabaseManager.initializeDatabase()

        val userRepository = UserRepository()
        authService = AuthService(userRepository)

        val autoLoginUser = authService.autoLogin()
        if (autoLoginUser != null) {
            showMainView(autoLoginUser)
        }
            showLoginView()
        }

    private fun showLoginView() {
        val loginView = LoginView(stage, authService) { user ->
            showMainView(user)
        }
        loginView.show()
    }
    private fun showMainView(user: User) {
        val mainView = MainView(stage, user) {
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
