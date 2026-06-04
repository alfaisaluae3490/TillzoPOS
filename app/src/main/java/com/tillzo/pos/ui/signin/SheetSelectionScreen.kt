package com.tillzo.pos.ui.signin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tillzo.pos.data.remote.SheetsRemoteDataSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetSelectionScreen(
    sheets: List<SheetsRemoteDataSource.ExistingSheetInfo>,
    onSheetSelected: (spreadsheetId: String) -> Unit,
    onCreateNew: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Your Data Sheet", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Info banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E88E5).copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "We found existing POS data sheets in your " +
                        "Google Drive. Select one to continue, or " +
                        "create a fresh sheet.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Your Existing Sheets:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            // Sheet list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(sheets) { sheet ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSheetSelected(sheet.spreadsheetId) },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.TableChart,
                                        null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        sheet.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Last modified: ${sheet.modifiedTime}",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    "Created: ${sheet.createdTime}",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                tint = Color(0xFF1E88E5)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Divider with text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.15f))
                Text(
                    "  or  ",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.15f))
            }

            Spacer(Modifier.height(12.dp))

            // Create new button
            OutlinedButton(
                onClick = onCreateNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    null,
                    tint = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Start Fresh (Create New Sheet)",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
