package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.user.model.User
import com.lsk.learningtracker.user.service.AuthService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage

class LoginView(
    private val stage: Stage,
    private val authService: AuthService,
    private val onLoginSuccess: (User) -> Unit
) {

    private val usernameField = TextField()
    private val passwordField = PasswordField()
    private val rememberMeCheckBox = CheckBox("자동 로그인")
    private val loginButton = Button("로그인")
    private val goToSignUpButton = Button("회원가입")
    private val messageLabel = Label()

    fun show() {
        val root = VBox(15.0).apply {
            padding = Insets(30.0)
            alignment = Pos.CENTER

            children.addAll(
                Label("학습 Tracker").apply {
                    style = "-fx-font-size: 24px; -fx-font-weight: bold;"
                },
                Label("Username"),
                usernameField.apply {
                    promptText = "4-20자, 영문/숫자"
                    prefWidth = 250.0
                },
                Label("Password"),
                passwordField.apply {
                    promptText = "8-20자"
                    prefWidth = 250.0
                },
                rememberMeCheckBox.apply {
                    style = "-fx-font-size: 12px;"
                },
                loginButton.apply {
                    prefWidth = 250.0
                    style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
                },
                goToSignUpButton.apply {
                    prefWidth = 250.0
                },
                messageLabel.apply {
                    style = "-fx-text-fill: red;"
                    maxWidth = 250.0
                }
            )
        }

        setupEventHandlers()

        val scene = Scene(root, 400.0, 450.0)
        stage.scene = scene
        stage.title = "로그인"
        stage.show()
    }

    private fun setupEventHandlers() {
        loginButton.setOnAction {
            handleLogin()
        }

        goToSignUpButton.setOnAction {
            showSignUpView()
        }

        passwordField.setOnAction {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val username = usernameField.text.trim()
        val password = passwordField.text
        val rememberMe = rememberMeCheckBox.isSelected

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Username과 Password를 입력해주세요.")
            return
        }

        try {
            val user = authService.login(username, password, rememberMe)
            showMessage("✅ 로그인 성공: ${user.username}님 환영합니다!", isError = false)
            onLoginSuccess(user)

        } catch (e: IllegalArgumentException) {
            showMessage("❌ ${e.message}")
        }
    }

    private fun showSignUpView() {
        val signUpView = SignUpView(stage, authService) {
            show()
        }
        signUpView.show()
    }

    private fun showMessage(message: String, isError: Boolean = true) {
        messageLabel.text = message
        messageLabel.style = if (isError) {
            "-fx-text-fill: red;"
        } else {
            "-fx-text-fill: green;"
        }
    }
}
