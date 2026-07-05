package com.thelightphone.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import com.thelightphone.sdk.ui.verticalGridUnitsAsDp

/**
 * A tappable label/value pair, matching the settings row used in the weather
 * example tool. [showEditIcon] adds the same pencil icon History uses, sat
 * right next to the value itself (not centered against the whole row), since
 * a bare label/value pair alone doesn't read as tappable.
 */
@Composable
fun SelectSettingRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEditIcon: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(vertical = 0.75f.gridUnitsAsDp()),
    ) {
        LightText(
            text = label,
            variant = LightTextVariant.Detail,
            lighten = true,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // Heading's larger line-height carries more built-in top leading
            // than ToggleSelectorRow's Subheading text, which otherwise makes
            // this look further from its label even with identical padding.
            // Nudge it up to match.
            modifier = Modifier.offset(y = (-0.15f).verticalGridUnitsAsDp()),
        ) {
            LightText(
                text = value,
                variant = LightTextVariant.Heading,
            )
            if (showEditIcon) {
                LightIcon(
                    icon = LightIcons.PENCIL,
                    size = 1.5f,
                    modifier = Modifier.padding(start = 1f.gridUnitsAsDp()),
                )
            }
        }
    }
}
