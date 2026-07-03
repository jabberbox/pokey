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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

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

    fun selectWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { setWeightUnit(lightContext.dataStore, unit) }
    }

    fun selectTimeFormat(format: TimeFormat) {
        viewModelScope.launch { setTimeFormat(lightContext.dataStore, format) }
    }

    fun shiftBeginningWeightWhole(amount: Int) = shiftBeginningWeight(amount.toDouble())

    fun shiftBeginningWeightTenth(tenths: Int) = shiftBeginningWeight(tenths / 10.0)

    private fun shiftBeginningWeight(amount: Double) {
        val newValue = roundToOneDecimal((beginningWeightValue.value + amount).coerceAtLeast(0.0))
        beginningWeightValue.value = newValue
        viewModelScope.launch { setBeginningWeight(lightContext.dataStore, newValue.displayToLbs(weightUnit.value)) }
    }

    fun shiftGoalWeightWhole(amount: Int) = shiftGoalWeight(amount.toDouble())

    fun shiftGoalWeightTenth(tenths: Int) = shiftGoalWeight(tenths / 10.0)

    private fun shiftGoalWeight(amount: Double) {
        val newValue = roundToOneDecimal((goalWeightValue.value + amount).coerceAtLeast(0.0))
        goalWeightValue.value = newValue
        viewModelScope.launch { setGoalWeight(lightContext.dataStore, newValue.displayToLbs(weightUnit.value)) }
    }

    private fun roundToOneDecimal(value: Double) = round(value * 10) / 10.0
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
                    ToggleSelectorRow(
                        optionOff = WeightUnit.LBS,
                        optionOn = WeightUnit.KG,
                        selected = weightUnit,
                        labelOff = WeightUnit.LBS.label,
                        labelOn = WeightUnit.KG.label,
                        onSelect = viewModel::selectWeightUnit,
                        modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                        title = "Units",
                    )

                    LightText(
                        text = "Beginning weight",
                        variant = LightTextVariant.Detail,
                        modifier = Modifier.padding(bottom = 0.25f.gridUnitsAsDp()),
                    )
                    WeightSpinnerRow(
                        weightValue = beginningWeightValue,
                        unitLabel = weightUnit.label,
                        onShiftWhole = viewModel::shiftBeginningWeightWhole,
                        onShiftTenth = viewModel::shiftBeginningWeightTenth,
                        centered = false,
                        modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                    )

                    LightText(
                        text = "Goal weight",
                        variant = LightTextVariant.Detail,
                        modifier = Modifier.padding(bottom = 0.25f.gridUnitsAsDp()),
                    )
                    WeightSpinnerRow(
                        weightValue = goalWeightValue,
                        unitLabel = weightUnit.label,
                        onShiftWhole = viewModel::shiftGoalWeightWhole,
                        onShiftTenth = viewModel::shiftGoalWeightTenth,
                        centered = false,
                        modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                    )

                    ToggleSelectorRow(
                        optionOff = TimeFormat.HOUR_12,
                        optionOn = TimeFormat.HOUR_24,
                        selected = timeFormat,
                        labelOff = TimeFormat.HOUR_12.label,
                        labelOn = TimeFormat.HOUR_24.label,
                        onSelect = viewModel::selectTimeFormat,
                        modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                        title = "Time Format",
                    )

                    Divider(modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()))

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
