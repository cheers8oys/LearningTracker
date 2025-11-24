package com.lsk.learningtracker.faceDetection.service

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class FaceDetectionService(
    private val onFaceDetected: () -> Unit,
    private val onFaceLost: () -> Unit,
    private val onError: (String) -> Unit
) {
    private var process: Process? = null
    private var monitorThread: Thread? = null
    private var isRunning = false

    fun start() {
        when {
            isRunning -> return
        }

        isRunning = true

        try {
            startPythonProcess()
            startMonitoring()
        } catch (e: Exception) {
            isRunning = false
            onError("Start failed: ${e.message}")
        }
    }

    private fun startPythonProcess() {
        val pythonScript = findPythonScript()
        val pythonExecutable = findPythonExecutable()

        process = ProcessBuilder(pythonExecutable, pythonScript)
            .redirectErrorStream(true)
            .start()
    }

    private fun findPythonExecutable(): String {
        val possibleCommands = listOf(
            "python3",
            "python",
            "/usr/bin/python3",
            "/usr/local/bin/python3",
            "/opt/homebrew/bin/python3"
        )

        for (cmd in possibleCommands) {
            try {
                val testProcess = ProcessBuilder(cmd, "--version").start()
                testProcess.waitFor()
                when {
                    testProcess.exitValue() == 0 -> return cmd
                }
            } catch (e: Exception) {
                continue
            }
        }

        return "python3"
    }

    private fun findPythonScript(): String {
        val resourcePath = this::class.java.classLoader
            .getResource("python/face_detector.py")

        when {
            resourcePath != null -> return resourcePath.path
        }

        val paths = listOf(
            "src/main/resources/python/face_detector.py",
            "resources/python/face_detector.py",
            "python/face_detector.py"
        )

        for (path in paths) {
            val file = File(path)
            when {
                file.exists() -> return file.absolutePath
            }
        }

        throw IllegalStateException("face_detector.py not found")
    }

    private fun startMonitoring() {
        monitorThread = Thread {
            process?.let { proc ->
                try {
                    BufferedReader(InputStreamReader(proc.inputStream)).use { reader ->
                        while (isRunning) {
                            val line = reader.readLine() ?: break
                            handleMessage(line.trim())
                        }
                    }
                } catch (e: Exception) {
                    // Silent fail
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun handleMessage(message: String) {
        when {
            message == "READY" -> {}
            message == "FACE_DETECTED" -> onFaceDetected()
            message == "FACE_LOST" -> onFaceLost()
            message.startsWith("ERROR:") -> onError(message.substring(6))
        }
    }

    fun stop() {
        isRunning = false
        process?.destroy()
        monitorThread?.interrupt()
        process = null
        monitorThread = null
    }
}
