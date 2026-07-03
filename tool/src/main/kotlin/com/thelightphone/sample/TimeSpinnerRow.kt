package com.thelightphone.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val HOUR_12_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h")
private val HOUR_24_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH")
private val MINUTE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("mm")
private val AM_PM_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("a")

/**
 * Independent hour/minute spinners, each stepping by one. In 12-hour mode an
 * AM/PM segment is shown too, letting you flip the meridiem directly instead
 * of cranking the hour all the way around; 24-hour mode has no AM/PM concept
 * so that segment is omitted.
 */
@Composable
fun TimeSpinnerRow(
    time: LocalTime,
    use24Hour: Boolean,
    onShiftHour: (Long) -> Unit,
    onShiftMinute: (Long) -> Unit,
    onToggleAmPm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        SpinnerSegment(
            value = (if (use24Hour) HOUR_24_FORMAT else HOUR_12_FORMAT).format(time),
            onIncrement = { onShiftHour(1) },
            onDecrement = { onShiftHour(-1) },
        )
        LightText(text = ":", variant = LightTextVariant.Copy)
        SpinnerSegment(
            value = MINUTE_FORMAT.format(time),
            onIncrement = { onShiftMinute(1) },
            onDecrement = { onShiftMinute(-1) },
        )
        if (!use24Hour) {
            SpinnerSegment(
                value = AM_PM_FORMAT.format(time),
                onIncrement = onToggleAmPm,
                onDecrement = onToggleAmPm,
            )
        }
    }
}
