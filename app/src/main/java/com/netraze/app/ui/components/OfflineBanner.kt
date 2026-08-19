package com.netraze.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.netraze.app.ui.theme.NetrazeTypography
import com.netraze.app.ui.theme.OfflineChipBackground
import com.netraze.app.ui.theme.OfflineChipText
import com.netraze.app.ui.theme.Spacing

/**
 * State-aware offline banner per Section 16 of UI/UX Restructuring Guidelines:
 * - If backend unavailable AND cached data exists: "Offline — showing saved data"
 * - If backend unavailable AND no cached data exists: "Backend unavailable — no saved data yet"
 */
@Composable
fun OfflineBanner(
    isOffline: Boolean,
    hasCachedData: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isOffline) return

    val message = if (hasCachedData) {
        "Offline — showing saved data"
    } else {
        "Backend unavailable — no saved data yet"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .background(color = OfflineChipBackground, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = "Offline Mode",
                tint = OfflineChipText
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = message,
                style = NetrazeTypography.bodySmall,
                color = OfflineChipText
            )
        }
    }
}
