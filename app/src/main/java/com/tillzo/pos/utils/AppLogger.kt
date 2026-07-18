package com.tillzo.pos.utils

import android.util.Log
import com.tillzo.pos.data.local.dao.LogDao
import com.tillzo.pos.data.local.entity.AppLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
}
