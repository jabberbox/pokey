package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.designVerticalPxToDp
import com.thelightphone.sdk.ui.gridUnitsAsDp

private const val UNDERLINE_THICKNESS_PX = 3f
private const val VALUE_TO_UNDERLINE_GAP_GRID_UNITS = 0.5f

/**
 * Same look and behavior as [com.thelightphone.sdk.ui.LightTextField], but the
 * underline is sized to the value/placeholder text itself instead of most of
 * the screen width — appropriate for a short numeric value like "210.0".
 */
@Composable
fun NarrowNumericField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LightThemeTokens.colors
    Column(modifier = modifier.fillMaxWidth()) {
        LightText(
            text = label,
            variant = LightTextVariant.Detail,
            modifier = Modifier.padding(top = 1f.gridUnitsAsDp()),
        )
        val isPlaceholder = value.isBlank()
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .clickable(onClick = onClick)
                .padding(top = 0.25f.gridUnitsAsDp()),
        ) {
            LightText(
                text = if (isPlaceholder) placeholder else value,
                variant = LightTextVariant.Copy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(VALUE_TO_UNDERLINE_GAP_GRID_UNITS.gridUnitsAsDp()))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(UNDERLINE_THICKNESS_PX.designVerticalPxToDp())
                    .background(colors.content),
            )
        }
    }
}
