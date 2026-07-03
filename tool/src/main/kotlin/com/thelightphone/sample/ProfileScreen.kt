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

class ProfileViewModel(
    private val lightContext: SealedLightContext,
) : LightViewModel<Unit>() {

    val beginningWeightText = MutableStateFlow("")
    val goalWeightText = MutableStateFlow("")
    val weightUnit = MutableStateFlow(WeightUnit.LBS)
    val timeFormat = MutableStateFlow(TimeFormat.HOUR_12)

    init {
        viewModelScope.launch {
            profileFlow(lightContext.dataStore).collect { profile ->
                weightUnit.value = profile.weightUnit
                timeFormat.value = profile.timeFormat
                beginningWeightText.value = profile.beginningWeightLbs
                    ?.let { "%.1f".format(it.lbsToDisplay(profile.weightUnit)) } ?: ""
                goalWeightText.value = profile.goalWeightLbs
                    ?.let { "%.1f".format(it.lbsToDisplay(profile.weightUnit)) } ?: ""
            }
        }
    }

    fun selectWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { setWeightUnit(lightContext.dataStore, unit) }
    }

    fun selectTimeFormat(format: TimeFormat) {
        viewModelScope.launch { setTimeFormat(lightContext.dataStore, format) }
    }

    fun setBeginningWeightText(value: String) {
        val enteredLbs = value.toDoubleOrNull()?.displayToLbs(weightUnit.value)
        viewModelScope.launch { setBeginningWeight(lightContext.dataStore, enteredLbs) }
    }

    fun setGoalWeightText(value: String) {
        val enteredLbs = value.toDoubleOrNull()?.displayToLbs(weightUnit.value)
        viewModelScope.launch { setGoalWeight(lightContext.dataStore, enteredLbs) }
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
        val beginningWeightText by viewModel.beginningWeightText.collectAsState()
        val goalWeightText by viewModel.goalWeightText.collectAsState()
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

                    NarrowNumericField(
                        label = "Beginning weight (${weightUnit.label})",
                        value = beginningWeightText,
                        placeholder = "e.g. 210.0",
                        onClick = {
                            navigateTo(
                                screenFactory = {
                                    NumericTextInputEditorScreen(
                                        it,
                                        EditorRequest(title = "Beginning weight (${weightUnit.label})", initialValue = beginningWeightText),
                                    )
                                },
                                resultCallback = { viewModel.setBeginningWeightText(it) },
                            )
                        },
                        modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                    )

                    NarrowNumericField(
                        label = "Goal weight (${weightUnit.label})",
                        value = goalWeightText,
                        placeholder = "e.g. 170.0",
                        onClick = {
                            navigateTo(
                                screenFactory = {
                                    NumericTextInputEditorScreen(
                                        it,
                                        EditorRequest(title = "Goal weight (${weightUnit.label})", initialValue = goalWeightText),
                                    )
                                },
                                resultCallback = { viewModel.setGoalWeightText(it) },
                            )
                        },
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
