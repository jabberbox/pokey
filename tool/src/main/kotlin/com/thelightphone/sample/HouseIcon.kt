package com.thelightphone.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * No house/home icon exists anywhere in the SDK's icon set, so this draws a
 * simple outlined house silhouette directly, matching the visual weight of
 * the other tab icons. Walls are taller than the roof and a door is included
 * so it reads clearly as a house rather than an arrow.
 */
@Composable
fun HouseIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(2f.gridUnitsAsDp())) {
        val w = size.width
        val h = size.height
        val strokeWidth = h * 0.07f

        val outline = Path().apply {
            moveTo(w * 0.5f, h * 0.08f) // peak
            lineTo(w * 0.9f, h * 0.42f) // roof right
            lineTo(w * 0.75f, h * 0.42f) // wall right top
            lineTo(w * 0.75f, h * 0.92f) // wall right bottom
            lineTo(w * 0.25f, h * 0.92f) // wall left bottom
            lineTo(w * 0.25f, h * 0.42f) // wall left top
            lineTo(w * 0.1f, h * 0.42f) // roof left
            close()
        }
        val door = Path().apply {
            moveTo(w * 0.43f, h * 0.92f)
            lineTo(w * 0.43f, h * 0.65f)
            lineTo(w * 0.57f, h * 0.65f)
            lineTo(w * 0.57f, h * 0.92f)
        }

        val strokeStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(path = outline, color = tint, style = strokeStyle)
        drawPath(path = door, color = tint, style = strokeStyle)
    }
}
