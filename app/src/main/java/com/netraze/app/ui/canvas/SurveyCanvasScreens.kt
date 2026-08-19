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
import androidx.compose.material3.MaterialTheme
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
import com.netraze.app.data.local.entity.SurveyEntity
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

    LaunchedEffect(surveyId) {
        viewModel.loadSurveyCanvasData(surveyId)
    }

    val survey = uiState.survey
    val modeText = survey?.mode?.replace("_", " ")?.uppercase() ?: "SURVEY"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        HeaderBar(
            title = survey?.title?.ifBlank { "Survey Canvas" } ?: "Survey Canvas",
            subtitle = "Mode: $modeText | Status: ${survey?.status ?: "in_progress"}",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(Spacing.md))

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

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.positions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(FormSurfaceBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No spatial positions recorded yet.\nPosition points will render on this canvas.",
                    style = NetrazeTypography.bodyMedium,
                    color = TextOnBlue
                )
            }
        } else {
            // Offline Map / Plan Interactive Canvas
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
                                // Hit-test tapped point
                                val canvasWidth = size.width
                                val canvasHeight = size.height

                                val hit = uiState.positions.find { posWithObs ->
                                    val pos = posWithObs.position
                                    val normX = pos.floorPlanX ?: pos.simpleMapX ?: 0.5
                                    val normY = pos.floorPlanY ?: pos.simpleMapY ?: 0.5
                                    val pointX = (normX * canvasWidth).toFloat()
                                    val pointY = (normY * canvasHeight).toFloat()
                                    val dist = (tapOffset.x - pointX) * (tapOffset.x - pointX) + (tapOffset.y - pointY) * (tapOffset.y - pointY)
                                    dist < 40 * 40 // 40px hit radius
                                }
                                hit?.let { selectedPosition = it }
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
                    LazyColumn(modifier = Modifier.height(200.dp)) {
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
