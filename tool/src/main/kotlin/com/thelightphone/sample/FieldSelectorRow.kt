package com.thelightphone.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * Tab-like row of field names; tapping one selects it. Each tab also shows a
 * live preview of that field's current value, so switching tabs doesn't hide
 * what was just entered elsewhere. The caller shows only the selected
 * field's editor below, so the page stays short instead of listing every
 * spinner at once.
 */
@Composable
fun <T> FieldSelectorRow(
    fields: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    value: ((T) -> String)? = null,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        fields.forEach { field ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(field) }
                    .padding(vertical = 0.5f.gridUnitsAsDp()),
            ) {
                LightText(
                    text = label(field),
                    variant = LightTextVariant.Subheading,
                    lighten = field != selected,
                    align = TextAlign.Center,
                )
                if (value != null) {
                    LightText(
                        text = value(field),
                        variant = LightTextVariant.Fine,
                        lighten = true,
                        align = TextAlign.Center,
                        modifier = Modifier.padding(top = 0.25f.gridUnitsAsDp()),
                    )
                }
            }
        }
    }
}
