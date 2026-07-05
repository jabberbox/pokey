package com.thelightphone.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.thelightphone.sdk.ui.LightSurfaceScheme
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.gridUnitsAsDp

private const val BOTTOMBAR_HEIGHT_UNITS = 4f
private const val ICON_SIZE_UNITS = 2f
private const val HORIZONTAL_PADDING_UNITS = 2f
private const val TOP_MARGIN_UNITS = 1f
private const val UNSELECTED_ALPHA = 0.5f

/** Google's "home" glyph (https://fonts.google.com/icons) — LightIcons has no house/home icon. */
private const val HOME_ICON_PATH = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"

/**
 * The Material glyph has a wide keyline margin baked into its 24x24 grid
 * (roofline/legs sit well inside the edges), so at the same box size it
 * reads noticeably smaller than Light's own icons, which fill their
 * viewBox almost edge to edge. Scaled up to match apparent size.
 */
private const val HOME_ICON_SIZE_UNITS = 2.6f

/**
 * Persistent tab bar shown on every screen. Unlike [com.thelightphone.sdk.ui.LightBottomBar] +
 * [com.thelightphone.sdk.ui.LightIcon], this renders icons with an explicit per-tab tint
 * (always full color, with the inactive tabs faded via alpha) since the SDK's icon APIs
 * always tint from the theme uniformly and don't expose a per-call override. The Home tab
 * has no LightIcons equivalent, so it falls back to a Google icon (see [GoogleIcon]).
 */
@Composable
fun BottomNavBar(
    current: AppTab,
    onNavigate: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = TOP_MARGIN_UNITS.gridUnitsAsDp())
            .height(BOTTOMBAR_HEIGHT_UNITS.gridUnitsAsDp())
            .padding(horizontal = HORIZONTAL_PADDING_UNITS.gridUnitsAsDp()),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTab.entries.forEach { tab ->
            val selected = tab == current
            val tint = LightThemeTokens.colors.content

            Box(
                modifier = Modifier
                    .height(BOTTOMBAR_HEIGHT_UNITS.gridUnitsAsDp())
                    .alpha(if (selected) 1f else UNSELECTED_ALPHA)
                    .clickable(enabled = !selected) { onNavigate(tab) },
                contentAlignment = Alignment.Center,
            ) {
                val icon = tab.icon
                if (icon == null) {
                    GoogleIcon(pathData = HOME_ICON_PATH, tint = tint, sizeUnits = HOME_ICON_SIZE_UNITS)
                } else {
                    val drawableId = when (LightThemeTokens.surfaceScheme) {
                        LightSurfaceScheme.Dark -> icon.darkModeResource
                        LightSurfaceScheme.Light -> icon.lightModeResource
                    }
                    Icon(
                        painter = painterResource(drawableId),
                        contentDescription = icon.name,
                        tint = tint,
                        modifier = Modifier.size(ICON_SIZE_UNITS.gridUnitsAsDp()),
                    )
                }
            }
        }
    }
}
