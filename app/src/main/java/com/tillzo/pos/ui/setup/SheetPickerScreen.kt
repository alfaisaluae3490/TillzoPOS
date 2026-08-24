package com.tillzo.pos.ui.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.remote.PosSheetInfo
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetPickerScreen(
    accessToken: String,
    shopName: String,
    onSheetReady: (spreadsheetId: String) -> Unit,
    viewModel: SheetPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val restoreState by viewModel.restoreState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // FIX (2026-08-07): no existing sheet found → run the Onboarding wizard
    // (new user setup) instead of silently creating a blank sheet. Existing
    // users with sheets skip onboarding entirely (simple login + restore).
    var showOnboarding by remember { mutableStateOf(false) }

    if (showOnboarding) {
        com.tillzo.pos.ui.setup.onboarding.OnboardingScreen(
            onComplete = {
                showOnboarding = false
                val prefs = com.tillzo.pos.data.local.prefs.AppSetupPrefs(context)
                val bizName = prefs.businessName.ifBlank { shopName }
                viewModel.createNewSheet(accessToken, bizName) { newId ->
                    onSheetReady(newId)
                }
            }
        )
        return
    }

    LaunchedEffect(Unit) {
        viewModel.loadExistingSheets(accessToken)
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect { id ->
            onSheetReady(id)
        }
    }

    // FIX (2026-08-06): navigate as soon as the initial restore completes.
    // Old code only checked restoreState once right after enqueue (worker runs
    // async) so a fresh sheet never navigated home — user stuck on picker.
    LaunchedEffect(restoreState) {
        if (restoreState is DeltaSyncManager.RestoreState.Success) {
            onSheetReady(viewModel.spreadsheetIdOrEmpty)
        }
    }

    Scaffold(
        containerColor = Color(0xFF1A1A1A),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Select Data Sheet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Where should your POS data be stored?",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->

        when (restoreState) {
            is DeltaSyncManager.RestoreState.Running -> {
                RestoreInProgressDialog(
                    state = restoreState as DeltaSyncManager.RestoreState.Running,
                    modifier = Modifier.padding(padding)
                )
            }
            is DeltaSyncManager.RestoreState.Failed -> {
                RestoreFailedDialog(
                    error = (restoreState as DeltaSyncManager.RestoreState.Failed).error,
                    onRetry = { viewModel.retryRestore() },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                when (val state = uiState) {

                    is SheetPickerViewModel.UiState.Loading -> {
                        FullScreenLoader("Initializing...")
                    }

                    is SheetPickerViewModel.UiState.CreatingNewSheet -> {
                        FullScreenLoader("Creating your data sheet & business folder...")
                    }

                    is SheetPickerViewModel.UiState.CreationSuccess -> {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Business Ready!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Sheet: ${state.sheetName}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Folder: ${state.folderName}",
                                        color = Color(0xFF1E88E5),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Opening POS...",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    is SheetPickerViewModel.UiState.Error -> {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(state.msg, color = Color.White,
                                    textAlign = TextAlign.Center)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.loadExistingSheets(accessToken)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E88E5)
                                    )
                                ) { Text("Retry") }
                            }
                        }
                    }

                    is SheetPickerViewModel.UiState.Ready -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                if (state.isSearching) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF1E88E5)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Searching your Google Drive...",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                if (state.sheets.isNotEmpty()) {
                                    item {
                                        Text(
                                            "\uD83D\uDCC2  Found in your Google Drive",
                                            color = Color(0xFF1E88E5),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    items(
                                        state.sheets,
                                        key = { it.spreadsheetId }
                                    ) { sheet ->
                                        SheetOptionCard(
                                            sheet = sheet,
                                            onClick = {
                                                viewModel.selectSheet(sheet)
                                            }
                                        )
                                    }
                                }

                                if (state.sheets.isEmpty() && !state.isSearching) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF2A2A2A)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(24.dp),
                                                horizontalAlignment =
                                                    Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    Icons.Default.SearchOff,
                                                    null,
                                                    tint = Color.White.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    "No existing sheets found",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    "Create a new one below",
                                                    color = Color.White.copy(alpha = 0.3f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HorizontalDivider(
                                            modifier = Modifier.weight(1f),
                                            color = Color.White.copy(alpha = 0.1f)
                                        )
                                        Text(
                                            "  OR  ",
                                            color = Color.White.copy(alpha = 0.3f),
                                            fontSize = 11.sp
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.weight(1f),
                                            color = Color.White.copy(alpha = 0.1f)
                                        )
                                    }
                                }

                                item {
                                    Text(
                                        "✨  Start Fresh",
                                        color = Color(0xFF1E88E5),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                item {
                                    CreateNewSheetCard(
                                        onClick = {
                                            // FIX (2026-08-07): new user → run Onboarding
                                            // wizard first (business profile), then create sheet
                                            showOnboarding = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestoreInProgressDialog(
    state: DeltaSyncManager.RestoreState.Running,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Restoring cloud database...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    state.status,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFF1E88E5),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "This may take up to a minute. Please do not close the app.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun RestoreFailedDialog(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Restore Failed",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    error,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5)
                    )
                ) { Text("Retry Restore") }
            }
        }
    }
}

// ── Sheet Option Card ──────────────────────────────────────
@Composable
fun SheetOptionCard(
    sheet: PosSheetInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (sheet.isTagged)
                Color(0xFF4CAF50).copy(alpha = 0.4f)
            else
                Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Color(0xFF4CAF50).copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TableChart,
                    null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sheet.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Modified: ${formatDriveDate(sheet.modifiedTime)}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    "Created: ${formatDriveDate(sheet.createdTime)}",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 11.sp
                )
                if (sheet.isTagged) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Verified,
                            null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Tillzo POS backup",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color(0xFF1E88E5),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Create New Sheet Card ───────────────────────────────────
@Composable
fun CreateNewSheetCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Color(0xFF1E88E5).copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Create New Sheet",
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    "Start with a blank data sheet in your Drive",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color(0xFF1E88E5),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Full Screen Loader ──────────────────────────────────────
@Composable
fun FullScreenLoader(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF1E88E5))
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

// ── Date formatter helper ───────────────────────────────────
fun formatDriveDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()
        )
        val outputFormat = SimpleDateFormat(
            "dd MMM yyyy", Locale.getDefault()
        )
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: return isoDate)
    } catch (e: Exception) {
        isoDate.take(10) // fallback: show YYYY-MM-DD
    }
}
