package com.thelightphone.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM")

/** Independent month/day/year spinners, each stepping by one. */
@Composable
fun DateSpinnerRow(
    date: LocalDate,
    onShiftMonth: (Long) -> Unit,
    onShiftDay: (Long) -> Unit,
    onShiftYear: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        SpinnerSegment(
            value = MONTH_FORMAT.format(date),
            onIncrement = { onShiftMonth(1) },
            onDecrement = { onShiftMonth(-1) },
        )
        SpinnerSegment(
            value = date.dayOfMonth.toString(),
            onIncrement = { onShiftDay(1) },
            onDecrement = { onShiftDay(-1) },
        )
        SpinnerSegment(
            value = date.year.toString(),
            onIncrement = { onShiftYear(1) },
            onDecrement = { onShiftYear(-1) },
        )
    }
}
