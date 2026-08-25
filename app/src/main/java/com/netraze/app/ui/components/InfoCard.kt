package com.netraze.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.netraze.app.ui.theme.CardShape
import com.netraze.app.ui.theme.SurfaceTranslucent
import com.netraze.app.ui.theme.SurfaceWhite

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    isHighEmphasis: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = if (isHighEmphasis) SurfaceWhite else SurfaceTranslucent

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isHighEmphasis) 4.dp else 0.dp,
                pressedElevation = if (isHighEmphasis) 2.dp else 0.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isHighEmphasis) 4.dp else 0.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}
