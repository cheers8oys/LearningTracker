package com.lsk.learningtracker.todo.timer

import com.lsk.learningtracker.utils.TimeFormatter
import javafx.animation.AnimationTimer
import javafx.scene.control.Label

class TodoTimerManager(
    private val timerLabel: Label,
    private val onTimerUpdate: (Int) -> Unit,
    private val onTimerStateChange: (Boolean) -> Unit,
    private val onError: (Exception) -> Unit
) {
    private var timerRunning = false
    private var startTimeNs: Long = 0L
    private var animationTimer: AnimationTimer? = null
    private var elapsedSeconds = 0

    fun start() {
        try {
            validateCanStart()
            executeStart()
        } catch (e: Exception) {
            handleError(e)
        }
    }

    fun pause(): Int {
        try {
            return executePause()
        } catch (e: Exception) {
            handleError(e)
            return elapsedSeconds
        }
    }

    fun reset(): Int {
        try {
            return executeReset()
        } catch (e: Exception) {
            handleError(e)
            return 0
        }
    }

    fun stop() {
        animationTimer?.stop()
        animationTimer = null
        timerRunning = false
        onTimerStateChange(false)
    }

    fun setElapsedSeconds(seconds: Int) {
        require(seconds >= 0) { "경과 시간은 0 이상이어야 합니다." }
        elapsedSeconds = seconds
        updateTimerDisplay(seconds)
    }

    fun isRunning(): Boolean = timerRunning

    private fun validateCanStart() {
        when {
            timerRunning -> throw TimerException.InvalidStateException("타이머가 이미 실행 중입니다.")
        }
    }

    private fun executeStart() {
        timerRunning = true
        startTimeNs = System.nanoTime()
        onTimerStateChange(true)

        animationTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                val delta = (now - startTimeNs) / 1_000_000_000
                updateTimerDisplay(elapsedSeconds + delta.toInt())
            }
        }.also { it.start() }
    }

    private fun executePause(): Int {
        when {
            !timerRunning -> return elapsedSeconds
        }

        timerRunning = false
        animationTimer?.stop()
        animationTimer = null

        val delta = calculateDelta()
        elapsedSeconds += delta

        updateTimerDisplay(elapsedSeconds)
        onTimerUpdate(elapsedSeconds)
        onTimerStateChange(false)
        return elapsedSeconds
    }

    private fun executeReset(): Int {
        stop()
        elapsedSeconds = 0
        updateTimerDisplay(0)
        onTimerUpdate(0)
        return 0
    }

    private fun calculateDelta(): Int {
        return ((System.nanoTime() - startTimeNs) / 1_000_000_000).toInt()
    }

    private fun updateTimerDisplay(seconds: Int) {
        timerLabel.text = TimeFormatter.formatSeconds(seconds)
    }

    private fun handleError(exception: Exception) {
        stop()
        onError(exception)
    }
}
