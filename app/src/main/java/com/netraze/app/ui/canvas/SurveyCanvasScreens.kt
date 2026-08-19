package com.netraze.app.ui.canvas

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextSecondary
import java.util.UUID

private fun checkIsEmulator(): Boolean {
    return (Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            "google_sdk" == Build.PRODUCT)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyCanvasScreen(
    viewModel: SurveyCanvasViewModel,
    surveyId: UUID,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPosition by remember { mutableStateOf<PositionWithObservations?>(null) }
    var showRawEvidenceDialog by remember { mutableStateOf(false) }
    val isEmulator = remember { checkIsEmulator() }

    LaunchedEffect(surveyId) {
        viewModel.loadSurveyCanvasData(surveyId)
    }

    val survey = uiState.survey
    val mode = survey?.mode ?: "location_survey"
    val modeText = mode.replace("_", " ").uppercase()
    val syncStateText = (survey?.syncState ?: "pending").uppercase()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = survey?.title?.ifBlank { "Survey Workspace" } ?: "Survey Workspace",
                            style = NetrazeTypography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mode: $modeText | Sync: $syncStateText | Status: ${survey?.status?.uppercase() ?: "IN_PROGRESS"}",
                            style = NetrazeTypography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Spatial Analytics Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Positions", style = NetrazeTypography.labelSmall, color = TextOnBlue)
                        Text(text = "${uiState.positions.size}", style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Observations", style = NetrazeTypography.labelSmall, color = TextOnBlue)
                        Text(text = "${uiState.totalObservationsCount}", style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Unique APs", style = NetrazeTypography.labelSmall, color = TextOnBlue)
                        Text(text = "${uiState.uniqueBssidCount}", style = NetrazeTypography.titleMedium, color = TextOnBlue, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Max RSSI", style = NetrazeTypography.labelSmall, color = TextOnBlue)
                        Text(
                            text = if (uiState.maxRssi != null) "${uiState.maxRssi} dBm" else "N/A",
                            style = NetrazeTypography.titleMedium,
                            color = TextOnBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Emulator Device Notification Banner
            if (isEmulator) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Rounded.Info, contentDescription = "Info", tint = TextOnBlue)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Wi-Fi field scanning is available on a physical Android device.",
                            style = NetrazeTypography.bodySmall,
                            color = TextOnBlue
                        )
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PrimaryButton(
                        text = if (isEmulator) "Field Scan (Physical Only)" else if (uiState.isScanning) "Scanning..." else "+ Scan Wi-Fi Here",
                        onClick = {
                            if (!isEmulator) {
                                viewModel.addPositionAndScan(surveyId = surveyId, mode = mode)
                            }
                        },
                        enabled = !isEmulator && !uiState.isScanning
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PrimaryButton(
                        text = "View Raw Evidence",
                        onClick = { showRawEvidenceDialog = true }
                    )
                }
            }

            if (uiState.isLoading || uiState.isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = if (uiState.isScanning) "Executing Wi-Fi scan cycle..." else "Loading data...",
                            style = NetrazeTypography.bodyMedium,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (uiState.positions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(FormSurfaceBlue)
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEmulator)
                            "Survey metadata and Room local persistence loaded.\nField Wi-Fi radio measurements require physical Android device hardware."
                        else
                            "No spatial positions recorded yet.\nTap '+ Scan Wi-Fi Here' to add sampling positions.",
                        style = NetrazeTypography.bodyMedium,
                        color = TextOnBlue
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.positions) { pos ->
                        val posLabel = pos.position.label ?: "Position"
                        val xVal = pos.position.floorPlanX ?: pos.position.simpleMapX ?: 0.0
                        val yVal = pos.position.floorPlanY ?: pos.position.simpleMapY ?: 0.0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPosition = pos
                                },
                            colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text(
                                    text = "$posLabel (X: $xVal, Y: $yVal)",
                                    style = NetrazeTypography.titleMedium,
                                    color = TextOnBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Observations recorded: ${pos.observations.size}",
                                    style = NetrazeTypography.bodySmall,
                                    color = TextOnBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRawEvidenceDialog) {
        AlertDialog(
            onDismissRequest = { showRawEvidenceDialog = false },
            title = { Text("Raw Wi-Fi Evidence", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                if (uiState.positions.isEmpty()) {
                    Text("No raw Wi-Fi evidence recorded for this survey.", style = NetrazeTypography.bodyMedium)
                } else {
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(uiState.positions.flatMap { it.observations }) { obs ->
                            val chVal = obs.channel ?: "N/A"
                            Column(modifier = Modifier.padding(vertical = Spacing.xs)) {
                                Text(text = "SSID: ${obs.ssid ?: "Hidden"} | BSSID: ${obs.bssid}", style = NetrazeTypography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(text = "RSSI: ${obs.rssiDbm} dBm | Freq: ${obs.frequencyMhz} MHz | Ch: $chVal", style = NetrazeTypography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRawEvidenceDialog = false }) {
                    Text("Close", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
