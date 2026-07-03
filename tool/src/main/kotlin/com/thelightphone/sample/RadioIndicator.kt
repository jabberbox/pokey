package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.gridUnitsAsDp

@Composable
fun RadioIndicator(selected: Boolean, modifier: Modifier = Modifier) {
    val color = LightThemeTokens.colors.content
    val size = 2f.gridUnitsAsDp()
    Box(
        modifier = modifier
            .size(size)
            .let {
                if (selected) it.background(color, CircleShape) else it.border(2.dp, color, CircleShape)
            },
    )
}
