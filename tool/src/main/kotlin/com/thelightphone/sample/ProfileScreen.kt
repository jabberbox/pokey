package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    init {
        viewModelScope.launch {
            profileFlow(lightContext.dataStore).collect { profile ->
                weightUnit.value = profile.weightUnit
                timeFormat.value = profile.timeFormat
                beginningWeightValue.value = (profile.beginningWeightLbs ?: DEFAULT_WEIGHT_LBS)
                    .lbsToDisplay(profile.weightUnit)
                goalWeightValue.value = (profile.goalWeightLbs ?: DEFAULT_WEIGHT_LBS)
                    .lbsToDisplay(profile.weightUnit)
            }
        }
    }

    fun toggleWeightUnit() {
        val next = if (weightUnit.value == WeightUnit.LBS) WeightUnit.KG else WeightUnit.LBS
        // NonCancellable: navigating away (e.g. tapping another tab right after
        // this) clears the ViewModel and cancels viewModelScope, which would
        // otherwise cut this write off before it reaches disk.
        viewModelScope.launch { withContext(NonCancellable) { setWeightUnit(lightContext.dataStore, next) } }
    }

    fun toggleTimeFormat() {
        val next = if (timeFormat.value == TimeFormat.HOUR_12) TimeFormat.HOUR_24 else TimeFormat.HOUR_12
        viewModelScope.launch { withContext(NonCancellable) { setTimeFormat(lightContext.dataStore, next) } }
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

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    SelectSettingRow(
                        label = "Units",
                        value = weightUnit.label,
                        onClick = viewModel::toggleWeightUnit,
                    )
                    SelectSettingRow(
                        label = "Beginning weight",
                        value = "%.1f %s".format(beginningWeightValue, weightUnit.label),
                        onClick = {
                            navigateTo(screenFactory = { ProfileWeightEditScreen(it, ProfileWeightField.BEGINNING) })
                        },
                    )
                    SelectSettingRow(
                        label = "Goal weight",
                        value = "%.1f %s".format(goalWeightValue, weightUnit.label),
                        onClick = {
                            navigateTo(screenFactory = { ProfileWeightEditScreen(it, ProfileWeightField.GOAL) })
                        },
                    )
                    SelectSettingRow(
                        label = "Time Format",
                        value = timeFormat.label,
                        onClick = viewModel::toggleTimeFormat,
                        modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
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

                BottomNavBar(current = AppTab.PROFILE, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
