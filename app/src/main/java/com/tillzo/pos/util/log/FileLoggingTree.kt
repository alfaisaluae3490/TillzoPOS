package com.tillzo.pos.util.log

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

class FileLoggingTree(
    private val logDir: File,
    private val maxFileSize: Long = 512 * 1024,
    private val maxFiles: Int = 5
) : Timber.Tree() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val timestamp = dateFormat.format(Date())
        val priorityTag = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "F"
            else -> "?"
        }
        val logLine = "$timestamp [$priorityTag] [${tag ?: "NO_TAG"}] $message${if (t != null) "\n${Log.getStackTraceString(t)}" else ""}\n"
        writeLog(logLine)
    }

    private fun writeLog(line: String) {
        try {
            if (!logDir.exists()) logDir.mkdirs()
            val current = File(logDir, "app.log")
            if (current.exists() && current.length() > maxFileSize) {
                rotateLogs()
            }
            FileWriter(current, true).use { it.append(line) }
        } catch (e: IOException) {
            Log.e("FileLoggingTree", "Failed to write log: ${e.message}")
        }
    }

    private fun rotateLogs() {
        try {
            val current = File(logDir, "app.log")
            if (!current.exists()) return

            for (i in maxFiles - 1 downTo 1) {
                val old = File(logDir, "app.$i.log")
                val newer = File(logDir, "app.${i + 1}.log")
                if (old.exists()) old.renameTo(newer)
            }
            val first = File(logDir, "app.1.log")
            current.renameTo(first)

            val newCurrent = File(logDir, "app.log")
            newCurrent.createNewFile()
        } catch (e: IOException) {
            Log.e("FileLoggingTree", "Log rotation failed: ${e.message}")
        }
    }
}
