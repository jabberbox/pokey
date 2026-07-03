package com.thelightphone.sample

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

/** Independent whole/tenths spinners, each stepping by one, in whatever unit [unitLabel] names. */
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
    }
}
