package com.thelightphone.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.thelightphone.sdk.ui.LightThemeTokens

private const val SAVE_ICON_PATH =
    "M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"

/** Google's "save" glyph (https://fonts.google.com/icons) — a floppy disk, the universal save icon. */
@Composable
fun rememberSaveIconPainter(tint: Color = LightThemeTokens.colors.content): Painter =
    rememberGoogleIconPainter(pathData = SAVE_ICON_PATH, tint = tint)
