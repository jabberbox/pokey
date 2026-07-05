package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SealedLightContext
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round

private const val DEFAULT_WEIGHT_LBS = 150.0

enum class ProfileWeightField(val title: String) {
    BEGINNING("Beginning Weight"),
    GOAL("Goal Weight"),
}

class ProfileWeightEditViewModel(
    private val lightContext: SealedLightContext,
    private val field: ProfileWeightField,
) : LightViewModel<Unit>() {

    val weightValue = MutableStateFlow(DEFAULT_WEIGHT_LBS)
    val weightUnit = MutableStateFlow(WeightUnit.LBS)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = profileFlow(lightContext.dataStore).first()
            weightUnit.value = profile.weightUnit
            val storedLbs = when (field) {
                ProfileWeightField.BEGINNING -> profile.beginningWeightLbs
                ProfileWeightField.GOAL -> profile.goalWeightLbs
            } ?: DEFAULT_WEIGHT_LBS
            weightValue.value = storedLbs.lbsToDisplay(profile.weightUnit)
        }
    }

    fun shiftWhole(amount: Int) = shift(amount.toDouble())

    fun shiftTenth(tenths: Int) = shift(tenths / 10.0)

    private fun shift(amount: Double) {
        weightValue.value = roundToOneDecimal((weightValue.value + amount).coerceAtLeast(0.0))
    }

    private fun roundToOneDecimal(value: Double) = round(value * 10) / 10.0

    fun save(onSaved: () -> Unit) {
        val weightLbs = weightValue.value.displayToLbs(weightUnit.value)
        viewModelScope.launch(Dispatchers.IO) {
            when (field) {
                ProfileWeightField.BEGINNING -> setBeginningWeight(lightContext.dataStore, weightLbs)
                ProfileWeightField.GOAL -> setGoalWeight(lightContext.dataStore, weightLbs)
            }
            withContext(Dispatchers.Main) { onSaved() }
        }
    }
}

class ProfileWeightEditScreen(
    sealedActivity: SealedLightActivity,
    private val field: ProfileWeightField,
) : LightScreen<Unit, ProfileWeightEditViewModel>(sealedActivity) {

    override val viewModelClass: Class<ProfileWeightEditViewModel>
        get() = ProfileWeightEditViewModel::class.java

    override fun createViewModel() = ProfileWeightEditViewModel(lightContext, field)

    @Composable
    override fun Content() {
        val weightValue by viewModel.weightValue.collectAsState()
        val weightUnit by viewModel.weightUnit.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text(field.title),
                    rightButton = LightBarButton.Icon(
                        painter = rememberCheckmarkPainter(),
                        contentDescription = "Save",
                        onClick = { viewModel.save(onSaved = { goBack() }) },
                    ),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                WeightSpinnerRow(
                    weightValue = weightValue,
                    unitLabel = weightUnit.label,
                    onShiftWhole = viewModel::shiftWhole,
                    onShiftTenth = viewModel::shiftTenth,
                    modifier = Modifier.padding(top = 2f.gridUnitsAsDp()),
                )

                BottomNavBar(current = AppTab.PROFILE, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
