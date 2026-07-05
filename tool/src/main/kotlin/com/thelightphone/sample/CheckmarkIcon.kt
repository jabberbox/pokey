package com.thelightphone.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import com.thelightphone.sdk.ui.LightThemeTokens

/**
 * [com.thelightphone.sdk.ui.LightIcons.ACCEPT] ("confirm") is actually a
 * hollow warning-triangle, not a checkmark, so it can't be used for "Save".
 * No proper checkmark exists in the SDK's icon set, so this draws one
 * directly, the same way [HouseIcon] fills in for the missing house icon.
 */
private class CheckmarkPainter(private val color: Color) : Painter() {
    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        val w = size.width
        val h = size.height
        val strokeWidth = h * 0.12f
        val path = Path().apply {
            moveTo(w * 0.08f, h * 0.55f)
            lineTo(w * 0.38f, h * 0.85f)
            lineTo(w * 0.92f, h * 0.18f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun rememberCheckmarkPainter(tint: Color = LightThemeTokens.colors.content): Painter =
    remember(tint) { CheckmarkPainter(tint) }
