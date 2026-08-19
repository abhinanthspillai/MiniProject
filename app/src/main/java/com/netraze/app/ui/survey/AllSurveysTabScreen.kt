package com.netraze.app.ui.survey

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netraze.app.data.local.entity.SurveyEntity
import com.netraze.app.ui.components.PrimaryButton
import com.netraze.app.ui.theme.FormSurfaceBlue
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.PrimaryBlue
import com.netraze.app.ui.theme.Spacing
import com.netraze.app.ui.theme.SurfaceLight
import com.netraze.app.ui.theme.TextOnBlue
import com.netraze.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSurveysTabScreen(
    viewModel: SurveyViewModel,
    onSurveyClick: (SurveyEntity) -> Unit,
    onStartSurveyClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAllSurveys()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Surveys", style = NetrazeTypography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "All Recorded Indoor Wi-Fi Surveys", style = NetrazeTypography.bodySmall, color = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.lg)
        ) {
            PrimaryButton(
                text = "+ Start New Survey",
                onClick = onStartSurveyClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (uiState.surveys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No surveys recorded yet.\nTap '+ Start New Survey' to begin.",
                        style = NetrazeTypography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(uiState.surveys) { survey ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSurveyClick(survey) },
                            colors = CardDefaults.cardColors(containerColor = FormSurfaceBlue)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (survey.title.isNotBlank()) survey.title else "Untitled Survey",
                                        style = NetrazeTypography.titleMedium,
                                        color = TextOnBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = survey.status.uppercase(),
                                        style = NetrazeTypography.labelMedium,
                                        color = TextOnBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    text = "Mode: ${survey.mode.replace("_", " ").uppercase()}",
                                    style = NetrazeTypography.bodySmall,
                                    color = TextOnBlue
                                )
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    text = "Sync State: ${survey.syncState.uppercase()}",
                                    style = NetrazeTypography.labelSmall,
                                    color = TextOnBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
