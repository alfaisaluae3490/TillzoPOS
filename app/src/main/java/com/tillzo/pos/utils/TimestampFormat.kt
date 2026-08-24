package com.tillzo.pos.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OVERNIGHT-AUDIT Phase 2a — user-friendly timestamp formatting.
 *
 * FIX (2026-08-23): DB stored UNIX epoch millis (e.g. 1787320858884) and the
 * same raw integers were uploaded to the Google Sheet — unreadable for humans.
 * All sheet-bound timestamps now go through [formatTs]:
 *     1787320858884 -> "2026-08-17 04:00 PM"   (YYYY-MM-DD hh:mm AM/PM)
 */
object TimestampFormat {

    private val sheetFmt = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)

    /** UNIX millis -> "2026-08-17 04:00 PM"; null/0 -> "" ; keeps text dates as-is. */
    fun formatTs(value: Any?): String = when (value) {
        null -> ""
        is Long -> if (value <= 0L) "" else sheetFmt.format(Date(value))
        is Int -> if (value <= 0) "" else sheetFmt.format(Date(value.toLong()))
        is Number -> {
            val v = value.toLong()
            if (v <= 0L) "" else sheetFmt.format(Date(v))
        }
        else -> value.toString()
    }
}

/** Convenience for sheet row builders inside SyncWorker etc. */
fun Any?.ts(): String = TimestampFormat.formatTs(this)
