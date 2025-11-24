package com.lsk.learningtracker.utils

object TimeFormatter {

    fun formatSeconds(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatSecondsWithLabel(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%d시간 %d분 %d초", hours, minutes, seconds)
            minutes > 0 -> String.format("%d분 %d초", minutes, seconds)
            else -> String.format("%d초", seconds)
        }
    }
}
