package com.tillzo.pos.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.tillzo.pos.data.local.dao.LogDao
import com.tillzo.pos.data.local.entity.AppLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val logDao: LogDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun logInfo(tag: String, message: String) {
        Log.d(tag, message)
        scope.launch {
            logDao.insertLog(AppLogEntity(tag = tag, logLevel = "INFO", message = message))
        }
    }

    fun logWarn(tag: String, message: String) {
        Log.w(tag, message)
        scope.launch {
            logDao.insertLog(AppLogEntity(tag = tag, logLevel = "WARN", message = message))
        }
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        val msg = if (throwable != null) {
            "$message\n${Log.getStackTraceString(throwable)}"
        } else {
            message
        }
        Log.e(tag, message, throwable)
        scope.launch {
            logDao.insertLog(AppLogEntity(tag = tag, logLevel = "ERROR", message = msg))
        }
    }

    fun logFatalBlocking(tag: String, message: String) {
        Log.e(tag, message)
        logDao.insertLogBlocking(AppLogEntity(tag = tag, logLevel = "FATAL", message = message))
    }

    fun exportLogsToFile(context: Context): File? {
        return try {
            val logs = logDao.getAllLogsSync()
            val content = buildString {
                appendLine("TillzoPOS System Logs")
                appendLine("Exported: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                appendLine("Total entries: ${logs.size}")
                appendLine("=".repeat(80))
                appendLine()
                for (log in logs) {
                    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                    appendLine("[$ts] [${log.logLevel}] [${log.tag}] ${log.message}")
                    appendLine("-".repeat(80))
                }
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val fileName = "TillzoPOS_Logs_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.txt"
            val file = File(downloadsDir, fileName)
            file.writeText(content)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                ))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))

            file
        } catch (e: Exception) {
            Log.e("AppLogger", "Export failed: ${e.message}", e)
            null
        }
    }
}
