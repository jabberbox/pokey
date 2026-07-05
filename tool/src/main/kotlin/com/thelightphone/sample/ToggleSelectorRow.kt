package com.thelightphone.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * A labeled on/off switch (the toggle-track-and-thumb icon from the SDK's
 * icon set) for choosing between exactly two options, e.g. lbs/kg or
 * 12hr/24hr. Tapping either label jumps straight to that option; tapping the
 * switch itself flips it.
 *
 * [LightIcons.TOGGLE_ON]'s thumb is actually the hollow circle on the left
 * and [LightIcons.TOGGLE_OFF]'s is the filled circle on the right, so they're
 * assigned here the other way round from what their names suggest: off/first
 * option shows the hollow-left icon, on/second option shows the filled-right
 * one.
 */
@Composable
fun <T> ToggleSelectorRow(
    optionOff: T,
    optionOn: T,
    selected: T,
    labelOff: String,
    labelOn: String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val isOn = selected == optionOn
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 0.75f.gridUnitsAsDp()),
    ) {
        if (title != null) {
            LightText(
                text = title,
                variant = LightTextVariant.Detail,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LightText(
                text = labelOff,
                variant = LightTextVariant.Subheading,
                lighten = isOn,
                modifier = Modifier
                    .clickable { onSelect(optionOff) }
                    .padding(end = 1f.gridUnitsAsDp()),
            )
            LightIcon(
                icon = if (isOn) LightIcons.TOGGLE_OFF else LightIcons.TOGGLE_ON,
                modifier = Modifier.clickable { onSelect(if (isOn) optionOff else optionOn) },
            )
            LightText(
                text = labelOn,
                variant = LightTextVariant.Subheading,
                lighten = !isOn,
                modifier = Modifier
                    .clickable { onSelect(optionOn) }
                    .padding(start = 1f.gridUnitsAsDp()),
            )
        }
    }
}
