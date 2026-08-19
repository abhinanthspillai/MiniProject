package com.netraze.app.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderBar(title: String, subtitle: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(text = title, style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = NetrazeTypography.bodySmall, color = TextSecondary)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
    )
}

@Composable
fun SurveyCanvasScreen(
    viewModel: SurveyCanvasViewModel,
    surveyId: UUID,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPosition by remember { mutableStateOf<PositionWithObservations?>(null) }
    var showRawEvidenceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(surveyId) {
        viewModel.loadSurveyCanvasData(surveyId)
    }

    val survey = uiState.survey
    val mode = survey?.mode ?: "location_survey"
    val modeText = mode.replace("_", " ").uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        HeaderBar(
            title = survey?.title?.ifBlank { "Survey Workspace" } ?: "Survey Workspace",
            subtitle = "Mode: $modeText | Status: ${survey?.status ?: "in_progress"}",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

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

        Spacer(modifier = Modifier.height(Spacing.md))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PrimaryButton(
                    text = if (uiState.isScanning) "Scanning..." else "+ Scan Wi-Fi Here",
                    onClick = {
                        viewModel.addPositionAndScan(surveyId = surveyId, mode = mode)
                    },
                    enabled = !uiState.isScanning
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                PrimaryButton(
                    text = "View Raw Evidence",
                    onClick = { showRawEvidenceDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

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
                    text = "No spatial positions recorded yet.\nTap '+ Scan Wi-Fi Here' or tap the canvas to add sampling positions.",
                    style = NetrazeTypography.bodyMedium,
                    color = TextOnBlue
                )
            }
        } else {
            // Interactive Sampling Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(FormSurfaceBlue)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.positions) {
                            detectTapGestures { tapOffset ->
                                val canvasWidth = size.width
                                val canvasHeight = size.height

                                // Check hit-test on existing position marker
                                val hit = uiState.positions.find { posWithObs ->
                                    val pos = posWithObs.position
                                    val normX = pos.floorPlanX ?: pos.simpleMapX ?: 0.5
                                    val normY = pos.floorPlanY ?: pos.simpleMapY ?: 0.5
                                    val pointX = (normX * canvasWidth).toFloat()
                                    val pointY = (normY * canvasHeight).toFloat()
                                    val dist = (tapOffset.x - pointX) * (tapOffset.x - pointX) + (tapOffset.y - pointY) * (tapOffset.y - pointY)
                                    dist < 40 * 40 // 40px hit radius
                                }

                                if (hit != null) {
                                    selectedPosition = hit
                                } else {
                                    // Tap to place new position at normalized coordinates
                                    val normX = (tapOffset.x / canvasWidth).toDouble().coerceIn(0.0, 1.0)
                                    val normY = (tapOffset.y / canvasHeight).toDouble().coerceIn(0.0, 1.0)
                                    viewModel.addPositionAndScan(surveyId = surveyId, mode = mode, x = normX, y = normY)
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    uiState.positions.forEach { posWithObs ->
                        val pos = posWithObs.position
                        val normX = pos.floorPlanX ?: pos.simpleMapX ?: 0.5
                        val normY = pos.floorPlanY ?: pos.simpleMapY ?: 0.5
                        val pointX = (normX * canvasWidth).toFloat()
                        val pointY = (normY * canvasHeight).toFloat()

                        // Draw spatial position marker
                        drawCircle(
                            color = Color(0xFF1E88E5),
                            radius = 24f,
                            center = Offset(pointX, pointY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = Offset(pointX, pointY)
                        )
                    }
                }
            }
        }
    }

    selectedPosition?.let { posWithObs ->
        PositionDetailsDialog(
            positionWithObs = posWithObs,
            onDismiss = { selectedPosition = null }
        )
    }

    if (showRawEvidenceDialog) {
        RawEvidenceDialog(
            positions = uiState.positions,
            onDismiss = { showRawEvidenceDialog = false }
        )
    }
}

@Composable
fun PositionDetailsDialog(
    positionWithObs: PositionWithObservations,
    onDismiss: () -> Unit
) {
    val pos = positionWithObs.position
    val labelText = pos.label ?: "Spatial Position"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = labelText, style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Captured Cycles: ${positionWithObs.cycles.size} | Total Observations: ${positionWithObs.observations.size}",
                    style = NetrazeTypography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                if (positionWithObs.observations.isEmpty()) {
                    Text(text = "No Wi-Fi observations recorded for this position.", style = NetrazeTypography.bodyMedium)
                } else {
                    LazyColumn(modifier = Modifier.height(220.dp)) {
                        items(positionWithObs.observations) { obs ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.xs),
                                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.sm)) {
                                    Text(
                                        text = obs.ssid ?: "Hidden Network",
                                        style = NetrazeTypography.titleMedium,
                                        color = TextOnBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "BSSID: ${obs.bssid} | RSSI: ${obs.rssiDbm} dBm",
                                        style = NetrazeTypography.bodySmall,
                                        color = TextOnBlue
                                    )
                                    Text(
                                        text = "Freq: ${obs.frequencyMhz} MHz | Channel: ${obs.channel ?: "N/A"}",
                                        style = NetrazeTypography.labelSmall,
                                        color = TextOnBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun RawEvidenceDialog(
    positions: List<PositionWithObservations>,
    onDismiss: () -> Unit
) {
    val allObservations = positions.flatMap { it.observations }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Collected Raw Evidence", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Total Observations: ${allObservations.size} | Unique APs: ${allObservations.map { it.bssid }.distinct().size}",
                    style = NetrazeTypography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                if (allObservations.isEmpty()) {
                    Text(text = "No raw Wi-Fi evidence collected yet.\nTap '+ Scan Wi-Fi Here' to perform a scan.", style = NetrazeTypography.bodyMedium)
                } else {
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(allObservations) { obs ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.xs),
                                colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.sm)) {
                                    Text(
                                        text = obs.ssid ?: "Hidden Network",
                                        style = NetrazeTypography.titleMedium,
                                        color = TextOnBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "BSSID: ${obs.bssid}",
                                        style = NetrazeTypography.bodySmall,
                                        color = TextOnBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Signal: ${obs.rssiDbm} dBm | Freq: ${obs.frequencyMhz} MHz | Ch: ${obs.channel ?: "N/A"}",
                                        style = NetrazeTypography.bodySmall,
                                        color = TextOnBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}
