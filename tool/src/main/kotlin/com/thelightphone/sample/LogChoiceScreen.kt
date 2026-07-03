package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp

class LogChoiceViewModel : LightViewModel<Unit>()

class LogChoiceScreen(
    sealedActivity: SealedLightActivity,
) : LightScreen<Unit, LogChoiceViewModel>(sealedActivity) {

    override val viewModelClass: Class<LogChoiceViewModel>
        get() = LogChoiceViewModel::class.java

    override fun createViewModel() = LogChoiceViewModel()

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    center = LightTopBarCenter.Text("Log"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    LightText(
                        text = "Log Shot",
                        variant = LightTextVariant.Subheading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navigateTo(screenFactory = { LogShotScreen(it) }) }
                            .padding(vertical = 1f.gridUnitsAsDp()),
                    )
                    Divider()
                    LightText(
                        text = "Log Weight",
                        variant = LightTextVariant.Subheading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navigateTo(screenFactory = { LogWeightScreen(it) }) }
                            .padding(vertical = 1f.gridUnitsAsDp()),
                    )
                }

                BottomNavBar(current = AppTab.LOG, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
