package com.tillzo.pos.ui.settings.options.logviewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.LogDao
import com.tillzo.pos.data.local.entity.AppLogEntity
import com.tillzo.pos.utils.AppLogger
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SystemLogsViewModel @Inject constructor(
    private val logDao: LogDao,
    private val appLogger: AppLogger,
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
                val file = appLogger.exportLogsToFile(context)
                if (file != null) {
                    _exportMessage.value = "Exported to Downloads/${file.name}"
                } else {
                    _exportMessage.value = "Export failed"
                }
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
