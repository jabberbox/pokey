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

/**
 * Odometer-style tens/ones/tenths spinners, in whatever unit [unitLabel]
 * names. The tens column steps by ten so a big jump (e.g. dialing in from a
 * default of 150 up to 300) doesn't take dozens of individual taps; both it
 * and the ones column drive the same [onShiftWhole] callback, just with a
 * different step size, so the two stay in sync automatically (going from 149
 * to 150 rolls the tens column over with no extra bookkeeping).
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
    val tens = whole / 10
    val ones = whole % 10
    val tenths = ((weightValue - whole) * 10).roundToInt()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start,
        modifier = modifier.fillMaxWidth(),
    ) {
        SpinnerSegment(
            value = tens.toString(),
            onIncrement = { onShiftWhole(10) },
            onDecrement = { onShiftWhole(-10) },
        )
        SpinnerSegment(
            value = ones.toString(),
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
