package com.thelightphone.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import kotlin.math.roundToInt

/**
 * A single whole/tenths spinner (matching the plain, two-segment look of
 * [DateSpinnerRow] and [TimeSpinnerRow]), flanked by small "-10"/"+10" jump
 * buttons so a big move (e.g. dialing a default of 150 up to 300) doesn't
 * take a hundred individual taps. The jump buttons are plain text rather
 * than another spinner column, so they read as shortcuts on the one number
 * rather than a second, ambiguous digit place.
 */
@Composable
fun WeightSpinnerRow(
    weightValue: Double,
    unitLabel: String,
    onShiftWhole: (Int) -> Unit,
    onShiftTenth: (Int) -> Unit,
    modifier: Modifier = Modifier,
    centered: Boolean = true,
) {
    val whole = weightValue.toInt()
    val tenths = ((weightValue - whole) * 10).roundToInt()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start,
        modifier = modifier.fillMaxWidth(),
    ) {
        LightText(
            text = "-10",
            variant = LightTextVariant.Detail,
            lighten = true,
            modifier = Modifier
                .clickable { onShiftWhole(-10) }
                .padding(end = 0.75f.gridUnitsAsDp()),
        )
        SpinnerSegment(
            value = whole.toString(),
            onIncrement = { onShiftWhole(1) },
            onDecrement = { onShiftWhole(-1) },
        )
        LightText(text = ".", variant = LightTextVariant.Copy)
        SpinnerSegment(
            value = tenths.toString(),
            onIncrement = { onShiftTenth(1) },
            onDecrement = { onShiftTenth(-1) },
        )
        LightText(
            text = unitLabel,
            variant = LightTextVariant.Copy,
            modifier = Modifier.padding(start = 0.5f.gridUnitsAsDp()),
        )
        LightText(
            text = "+10",
            variant = LightTextVariant.Detail,
            lighten = true,
            modifier = Modifier
                .clickable { onShiftWhole(10) }
                .padding(start = 0.75f.gridUnitsAsDp()),
        )
    }
}
