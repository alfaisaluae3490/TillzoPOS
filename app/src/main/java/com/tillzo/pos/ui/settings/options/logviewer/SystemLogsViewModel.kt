package com.tillzo.pos.ui.settings.options.logviewer

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.LogDao
import com.tillzo.pos.data.local.entity.AppLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SystemLogsViewModel @Inject constructor(
    private val logDao: LogDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedLevel = MutableStateFlow<String?>(null)
    val selectedLevel: StateFlow<String?> = _selectedLevel.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allTags: StateFlow<List<String>> = logDao.getAllTags()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    val filteredLogs: StateFlow<List<AppLogEntity>> = combine(
        _selectedLevel, _selectedTag, _searchQuery
    ) { level, tag, query ->
        FilterParams(level, tag, query)
    }.flatMapLatest { params ->
        if (params.tag == null && params.level == null && params.query.isBlank()) {
            logDao.getAllLogs()
        } else {
            logDao.getFilteredLogs(params.tag, params.level)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setLevelFilter(level: String?) {
        _selectedLevel.value = level
    }

    fun setTagFilter(tag: String?) {
        _selectedTag.value = tag
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun exportLogs() {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            try {
                val logs = logDao.getAllLogsSync()
                val content = buildString {
                    appendLine("TillzoPOS System Logs")
                    appendLine("Exported: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                    appendLine("Total entries: ${logs.size}")
                    appendLine("=" .repeat(80))
                    appendLine()
                    for (log in logs) {
                        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                        appendLine("[$ts] [${log.logLevel}] [${log.tag}] ${log.message}")
                        appendLine("-" .repeat(80))
                    }
                }

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val fileName = "TillzoPOS_Logs_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.txt"
                val file = File(downloadsDir, fileName)
                file.writeText(content)

                _exportMessage.value = "Exported to Downloads/$fileName"
            } catch (e: Exception) {
                _exportMessage.value = "Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    private data class FilterParams(
        val level: String?,
        val tag: String?,
        val query: String
    )
}
