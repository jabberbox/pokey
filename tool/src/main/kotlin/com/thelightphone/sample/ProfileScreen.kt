package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import io.github.jabberbox.pokey.BuildConfig
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SealedLightContext
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_WEIGHT_LBS = 150.0

class ProfileViewModel(
    private val lightContext: SealedLightContext,
) : LightViewModel<Unit>() {

    val beginningWeightValue = MutableStateFlow(DEFAULT_WEIGHT_LBS)
    val goalWeightValue = MutableStateFlow(DEFAULT_WEIGHT_LBS)
    val weightUnit = MutableStateFlow(WeightUnit.LBS)
    val timeFormat = MutableStateFlow(TimeFormat.HOUR_12)
    val enabledBodyParts = MutableStateFlow(BodyPart.entries.toSet())

    init {
        viewModelScope.launch {
            profileFlow(lightContext.dataStore).collect { profile ->
                weightUnit.value = profile.weightUnit
                timeFormat.value = profile.timeFormat
                enabledBodyParts.value = profile.enabledBodyParts
                beginningWeightValue.value = (profile.beginningWeightLbs ?: DEFAULT_WEIGHT_LBS)
                    .lbsToDisplay(profile.weightUnit)
                goalWeightValue.value = (profile.goalWeightLbs ?: DEFAULT_WEIGHT_LBS)
                    .lbsToDisplay(profile.weightUnit)

                // The fallback above is only ever a display value. If it's
                // never actually written, it can look identical to a real,
                // already-set value (e.g. a goal of 150 looks the same
                // whether it's genuinely set or just the placeholder) while
                // Home's "N lbs to goal" line silently never shows, since it
                // treats null as "no goal set". Persist it as soon as it's
                // first shown, so viewing Profile alone is enough -- no need
                // to drill into the edit screen just to make it register.
                if (profile.beginningWeightLbs == null) {
                    withContext(NonCancellable) { setBeginningWeight(lightContext.dataStore, DEFAULT_WEIGHT_LBS) }
                }
                if (profile.goalWeightLbs == null) {
                    withContext(NonCancellable) { setGoalWeight(lightContext.dataStore, DEFAULT_WEIGHT_LBS) }
                }
            }
        }
    }

    fun selectWeightUnit(unit: WeightUnit) {
        // NonCancellable: navigating away (e.g. tapping another tab right after
        // this) clears the ViewModel and cancels viewModelScope, which would
        // otherwise cut this write off before it reaches disk.
        viewModelScope.launch { withContext(NonCancellable) { setWeightUnit(lightContext.dataStore, unit) } }
    }

    fun selectTimeFormat(format: TimeFormat) {
        viewModelScope.launch { withContext(NonCancellable) { setTimeFormat(lightContext.dataStore, format) } }
    }

    fun toggleBodyPart(bodyPart: BodyPart) {
        val enabled = bodyPart !in enabledBodyParts.value
        viewModelScope.launch {
            withContext(NonCancellable) { setBodyPartEnabled(lightContext.dataStore, bodyPart, enabled) }
        }
    }
}

class ProfileScreen(
    sealedActivity: SealedLightActivity,
) : LightScreen<Unit, ProfileViewModel>(sealedActivity) {

    override val viewModelClass: Class<ProfileViewModel>
        get() = ProfileViewModel::class.java

    override fun createViewModel() = ProfileViewModel(lightContext)

    @Composable
    override fun Content() {
        val beginningWeightValue by viewModel.beginningWeightValue.collectAsState()
        val goalWeightValue by viewModel.goalWeightValue.collectAsState()
        val weightUnit by viewModel.weightUnit.collectAsState()
        val timeFormat by viewModel.timeFormat.collectAsState()
        val enabledBodyParts by viewModel.enabledBodyParts.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    center = LightTopBarCenter.Text("Profile"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    LightScrollView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 1f.gridUnitsAsDp()),
                    ) {
                        ToggleSelectorRow(
                            optionOff = WeightUnit.LBS,
                            optionOn = WeightUnit.KG,
                            selected = weightUnit,
                            labelOff = WeightUnit.LBS.label,
                            labelOn = WeightUnit.KG.label,
                            onSelect = viewModel::selectWeightUnit,
                            title = "Units",
                        )
                        SelectSettingRow(
                            label = "Beginning weight",
                            value = "%.1f %s".format(beginningWeightValue, weightUnit.label),
                            onClick = {
                                navigateTo(screenFactory = { ProfileWeightEditScreen(it, ProfileWeightField.BEGINNING) })
                            },
                            showEditIcon = true,
                        )
                        SelectSettingRow(
                            label = "Goal weight",
                            value = "%.1f %s".format(goalWeightValue, weightUnit.label),
                            onClick = {
                                navigateTo(screenFactory = { ProfileWeightEditScreen(it, ProfileWeightField.GOAL) })
                            },
                            showEditIcon = true,
                        )
                        ToggleSelectorRow(
                            optionOff = TimeFormat.HOUR_12,
                            optionOn = TimeFormat.HOUR_24,
                            selected = timeFormat,
                            labelOff = TimeFormat.HOUR_12.label,
                            labelOn = TimeFormat.HOUR_24.label,
                            onSelect = viewModel::selectTimeFormat,
                            title = "Time Format",
                        )

                        LightText(
                            text = "Pokey (Build ${BuildConfig.VERSION_NAME})",
                            variant = LightTextVariant.Detail,
                            lighten = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 1f.gridUnitsAsDp()),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 1f.gridUnitsAsDp()),
                    ) {
                        LightText(
                            text = "Injection Sites",
                            variant = LightTextVariant.Detail,
                            lighten = true,
                            modifier = Modifier.padding(bottom = 0.5f.gridUnitsAsDp()),
                        )
                        BodyPart.entries.forEach { part ->
                            val enabled = part in enabledBodyParts
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleBodyPart(part) }
                                    .padding(vertical = 0.45f.gridUnitsAsDp()),
                            ) {
                                RadioIndicator(
                                    selected = enabled,
                                    modifier = Modifier.padding(end = 1f.gridUnitsAsDp()),
                                )
                                LightText(
                                    text = part.label,
                                    variant = LightTextVariant.Copy,
                                    lighten = !enabled,
                                )
                            }
                        }
                    }
                }

                BottomNavBar(current = AppTab.PROFILE, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
