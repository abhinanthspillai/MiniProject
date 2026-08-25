package com.netraze.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val NetrazeShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

val TopSheetShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
val CardShape = RoundedCornerShape(32.dp)
val InputShape = RoundedCornerShape(20.dp)
val ButtonShape = RoundedCornerShape(50) // Pill shape
