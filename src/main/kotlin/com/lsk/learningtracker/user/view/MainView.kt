package com.lsk.learningtracker.user.view

import com.lsk.learningtracker.user.model.User
import javafx.stage.Stage
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class MainView(
    private val stage: Stage,
    private val user: User,
    private val onLogout: () -> Unit
) {

    fun show() {
        val root = VBox(20.0).apply {
            padding = Insets(30.0)
            alignment = Pos.CENTER

            children.addAll(
                Label("환영합니다!").apply {
                    style = "-fx-font-size: 24px; -fx-font-weight: bold;"
                },
                Label("로그인 사용자: ${user.username}").apply {
                    style = "-fx-font-size: 16px;"
                },
                Label("TODO: 투두리스트 기능 추가 예정").apply {
                    style = "-fx-font-size: 14px; -fx-text-fill: gray;"
                },
                Button("로그아웃").apply {
                    prefWidth = 200.0
                    style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;"
                    setOnAction {
                        handleLogout()
                    }
                }
            )
        }

        val scene = Scene(root, 400.0, 300.0)
        stage.scene = scene
        stage.title = "학습 Tracker - ${user.username}"
        stage.show()
    }

    private fun handleLogout() {
        println("✅ ${user.username} 로그아웃")
        onLogout()
    }
}