package com.thelightphone.sample

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
import com.thelightphone.sdk.ui.lightClickable

/**
 * A tappable label/value pair, matching the settings row used in the weather
 * example tool. [showEditIcon] adds the same pencil icon History uses on its
 * rows, since a bare label/value pair alone doesn't read as tappable.
 */
@Composable
fun SelectSettingRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEditIcon: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(vertical = 0.75f.gridUnitsAsDp()),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            LightText(
                text = label,
                variant = LightTextVariant.Detail,
                lighten = true,
            )
            LightText(
                text = value,
                variant = LightTextVariant.Heading,
            )
        }
        if (showEditIcon) {
            LightIcon(
                icon = LightIcons.PENCIL,
                size = 1.5f,
                modifier = Modifier.padding(start = 0.5f.gridUnitsAsDp()),
            )
        }
    }
}
