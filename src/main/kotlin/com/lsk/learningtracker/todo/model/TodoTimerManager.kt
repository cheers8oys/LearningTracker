package com.lsk.learningtracker.todo.timer

import com.lsk.learningtracker.utils.TimeFormatter
import javafx.animation.AnimationTimer
import javafx.scene.control.Label

class TodoTimerManager(
    private val timerLabel: Label,
    private val onTimerUpdate: (Int) -> Unit,
    private val onTimerStateChange: (Boolean) -> Unit
) {
    private var timerRunning = false
    private var startTimeNs: Long = 0L
    private var animationTimer: AnimationTimer? = null
    private var elapsedSeconds = 0

    fun start() {
        if (timerRunning) return

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

    fun pause(): Int {
        if (!timerRunning) return elapsedSeconds

        timerRunning = false
        animationTimer?.stop()
        animationTimer = null

        val delta = ((System.nanoTime() - startTimeNs) / 1_000_000_000).toInt()
        elapsedSeconds += delta

        updateTimerDisplay(elapsedSeconds)
        onTimerUpdate(elapsedSeconds)
        onTimerStateChange(false)
        return elapsedSeconds
    }

    fun reset(): Int {
        stop()
        elapsedSeconds = 0
        updateTimerDisplay(0)
        onTimerUpdate(0)
        return 0
    }

    fun stop() {
        animationTimer?.stop()
        animationTimer = null
        timerRunning = false
        onTimerStateChange(false)
    }

    fun setElapsedSeconds(seconds: Int) {
        elapsedSeconds = seconds
        updateTimerDisplay(seconds)
    }

    fun isRunning(): Boolean = timerRunning

    private fun updateTimerDisplay(seconds: Int) {
        timerLabel.text = TimeFormatter.formatSeconds(seconds)
    }
}
