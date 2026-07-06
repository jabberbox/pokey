package com.thelightphone.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * A gauge-style half-circle (like a speedometer) tracking the week between
 * shots: it starts empty right after a shot and fills in white, clockwise
 * from the left, as the week elapses, reaching full at day 7. The dim gray
 * track underneath is always a full semicircle so the remaining gap is
 * visible at a glance.
 */
@Composable
fun NextDoseArc(
    progress: Float,
    primaryText: String,
    secondaryText: String,
    tertiaryText: String,
    modifier: Modifier = Modifier,
) {
    // Same treatment as the bottom nav's unselected tabs: full color at half
    // alpha, not the separate contentSecondary gray token, so this and the
    // nav bar read as the same "dimmed" gray.
    val progressColor = LightThemeTokens.colors.content
    val trackColor = progressColor.copy(alpha = 0.5f)
    // gridUnitsAsDp() is @Composable (reads LocalConfiguration), so it has to
    // be resolved here and handed into the Canvas as a plain Dp -- the draw
    // lambda below isn't a composable context.
    val strokeWidthDp = 0.72f.gridUnitsAsDp()

    Box(
        modifier = modifier
            .width(13.2f.gridUnitsAsDp())
            .height(7.2f.gridUnitsAsDp()),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidthDp.toPx()
            val diameter = size.width - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val arcSize = Size(diameter, diameter)
            val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

            // Android's arc angles run clockwise with 0deg at 3-o'clock, so
            // (confusingly) 90deg lands at 6-o'clock, not 12. To sweep the
            // *upper* half from the 9-o'clock point, over the top, to the
            // 3-o'clock point, the angle has to increase from 180 to 360 --
            // i.e. a positive sweep, not negative.
            drawArc(
                color = trackColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = stroke,
                topLeft = topLeft,
                size = arcSize,
            )
            drawArc(
                color = progressColor,
                startAngle = 180f,
                sweepAngle = 180f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = stroke,
                topLeft = topLeft,
                size = arcSize,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 0.15f.gridUnitsAsDp()),
        ) {
            LightText(text = primaryText, variant = LightTextVariant.Heading, align = TextAlign.Center)
            LightText(text = secondaryText, variant = LightTextVariant.Copy, align = TextAlign.Center)
            LightText(
                text = tertiaryText,
                variant = LightTextVariant.Detail,
                align = TextAlign.Center,
                lighten = true,
                modifier = Modifier.padding(top = 0.15f.gridUnitsAsDp()),
            )
        }
    }
}
