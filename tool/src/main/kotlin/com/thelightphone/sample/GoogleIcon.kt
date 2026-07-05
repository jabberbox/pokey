package com.thelightphone.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.PathParser
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * Renders a Material Symbols glyph (https://fonts.google.com/icons) directly
 * from its raw 24x24 SVG path data, for wherever Light's own icon set
 * (LightIcons) doesn't have a matching icon. Used instead of hand-drawing a
 * shape, so the result matches an actual published icon rather than our own
 * approximation of one.
 */
private class GoogleIconPainter(pathData: String, private val color: Color) : Painter() {
    private val path = PathParser().parsePathString(pathData).toPath()
    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        val scaleFactor = size.width / 24f
        scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
            drawPath(path = path, color = color)
        }
    }
}

@Composable
fun rememberGoogleIconPainter(pathData: String, tint: Color): Painter =
    remember(pathData, tint) { GoogleIconPainter(pathData, tint) }

@Composable
fun GoogleIcon(
    pathData: String,
    tint: Color,
    modifier: Modifier = Modifier,
    sizeUnits: Float = 2f,
) {
    Image(
        painter = rememberGoogleIconPainter(pathData, tint),
        contentDescription = null,
        modifier = modifier.size(sizeUnits.gridUnitsAsDp()),
    )
}
