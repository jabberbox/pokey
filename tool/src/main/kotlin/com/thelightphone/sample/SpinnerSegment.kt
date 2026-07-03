package com.thelightphone.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

/** Up arrow / value / down arrow, each tap changing the value by one unit. */
@Composable
fun SpinnerSegment(
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 0.75f.gridUnitsAsDp()),
    ) {
        LightIcon(
            icon = LightIcons.UP,
            modifier = Modifier.clickable { onIncrement() },
        )
        LightText(
            text = value,
            variant = LightTextVariant.Copy,
            modifier = Modifier.padding(vertical = 0.25f.gridUnitsAsDp()),
        )
        LightIcon(
            icon = LightIcons.DOWN,
            modifier = Modifier.clickable { onDecrement() },
        )
    }
}
