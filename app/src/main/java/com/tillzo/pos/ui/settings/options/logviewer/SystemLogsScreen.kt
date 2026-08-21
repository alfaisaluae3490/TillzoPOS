package com.tillzo.pos.ui.settings.options.logviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.data.local.entity.AppLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemLogsScreen(
    onBack: () -> Unit,
    viewModel: SystemLogsViewModel = hiltViewModel()
) {
    val filteredLogs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val selectedLevel by viewModel.selectedLevel.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var expandedLogIds by remember { mutableStateOf(setOf<Long>()) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    val logLevels = listOf(null, "INFO", "WARN", "ERROR", "FATAL")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("System Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(
                        onClick = { viewModel.exportLogs() },
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Download, contentDescription = "Export")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Filter by message...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Active filters row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedLevel != null) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.setLevelFilter(null) },
                        label = { Text(selectedLevel!!, fontSize = 11.sp) },
                        trailingIcon = { Text("x", fontSize = 11.sp) }
                    )
                }
                if (selectedTag != null) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.setTagFilter(null) },
                        label = { Text(selectedTag!!, fontSize = 11.sp) },
                        trailingIcon = { Text("x", fontSize = 11.sp) }
                    )
                }
            }

            // Logs list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLogs, key = { it.logId }) { log ->
                    LogEntryCard(
                        log = log,
                        isExpanded = log.logId in expandedLogIds,
                        onToggleExpand = {
                            expandedLogIds = if (log.logId in expandedLogIds) {
                                expandedLogIds - log.logId
                            } else {
                                expandedLogIds + log.logId
                            }
                        }
                    )
                }

                if (filteredLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No logs found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Filter Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                Text("Log Level", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    logLevels.forEach { level ->
                        FilterChip(
                            selected = selectedLevel == level,
                            onClick = { viewModel.setLevelFilter(level) },
                            label = {
                                Text(
                                    level ?: "ALL",
                                    fontSize = 12.sp,
                                    color = when (level) {
                                        "ERROR" -> Color(0xFFF44336)
                                        "FATAL" -> Color(0xFFD32F2F)
                                        "WARN" -> Color(0xFFFF9800)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Tag", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { viewModel.setTagFilter(null) },
                            label = { Text("ALL", fontSize = 12.sp) }
                        )
                    }
                    items(allTags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { viewModel.setTagFilter(tag) },
                            label = { Text(tag, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(
    log: AppLogEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val isError = log.logLevel in listOf("ERROR", "FATAL")

    val levelColor = when {
        isError -> MaterialTheme.colorScheme.onErrorContainer
        log.logLevel == "WARN" -> Color(0xFFFF9800)
        log.logLevel == "INFO" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val primaryTextColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val secondaryTextColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.logLevel,
                        color = levelColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = log.tag,
                        color = secondaryTextColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = formatLogTimestamp(log.timestamp),
                    color = secondaryTextColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = log.message,
                color = primaryTextColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(log.message))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Copy Log", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun formatLogTimestamp(ts: Long): String {
    return try {
        SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts))
    } catch (e: Exception) {
        ts.toString()
    }
}
