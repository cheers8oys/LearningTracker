package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.user.service.AuthService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage

class SignUpView(
    private val stage: Stage,
    private val authService: AuthService,
    private val onBackToLogin: () -> Unit
) {

    private val usernameField = TextField()
    private val passwordField = PasswordField()
    private val confirmPasswordField = PasswordField()
    private val signUpButton = Button("회원가입")
    private val backButton = Button("로그인으로 돌아가기")
    private val messageLabel = Label()

    fun show() {
        val root = VBox(15.0).apply {
            padding = Insets(30.0)
            alignment = Pos.CENTER

            children.addAll(
                Label("회원가입").apply {
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
                Label("Password 확인"),
                confirmPasswordField.apply {
                    promptText = "비밀번호 재입력"
                    prefWidth = 250.0
                },
                signUpButton.apply {
                    prefWidth = 250.0
                    style = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;"
                },
                backButton.apply {
                    prefWidth = 250.0
                },
                messageLabel.apply {
                    style = "-fx-text-fill: red;"
                    maxWidth = 250.0
                }
            )
        }

        setupEventHandlers()

        val scene = Scene(root, 400.0, 500.0)
        stage.scene = scene
        stage.title = "회원가입"
        stage.show()
    }

    private fun setupEventHandlers() {
        signUpButton.setOnAction {
            handleSignUp()
        }

        backButton.setOnAction {
            onBackToLogin()
        }

        // Enter 키로 회원가입
        confirmPasswordField.setOnAction {
            handleSignUp()
        }
    }

    private fun handleSignUp() {
        val username = usernameField.text.trim()
        val password = passwordField.text
        val confirmPassword = confirmPasswordField.text

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("모든 필드를 입력해주세요.")
            return
        }

        try {
            authService.register(username, password, confirmPassword)
            showMessage("✅ 회원가입 성공!", isError = false)

            javafx.application.Platform.runLater {
                Thread.sleep(1000)
                onBackToLogin()
            }

        } catch (e: IllegalArgumentException) {
            showMessage("❌ ${e.message}")
        }
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