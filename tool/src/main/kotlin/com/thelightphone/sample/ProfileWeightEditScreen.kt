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
import kotlinx.coroutines.NonCancellable
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
            }
            val resolvedLbs = storedLbs ?: DEFAULT_WEIGHT_LBS
            weightValue.value = resolvedLbs.lbsToDisplay(profile.weightUnit)
            if (storedLbs == null) {
                // Nothing was actually stored yet -- the placeholder is only
                // ever a display fallback, so if it happens to already match
                // what the user wants, they'd never touch a spinner and it
                // would never get saved. Persist it immediately so viewing
                // this screen always establishes a real value.
                withContext(NonCancellable) {
                    when (field) {
                        ProfileWeightField.BEGINNING -> setBeginningWeight(lightContext.dataStore, resolvedLbs)
                        ProfileWeightField.GOAL -> setGoalWeight(lightContext.dataStore, resolvedLbs)
                    }
                }
            }
        }
    }

    fun shiftWhole(amount: Int) = shift(amount.toDouble())

    fun shiftTenth(tenths: Int) = shift(tenths / 10.0)

    private fun shift(amount: Double) {
        val newValue = roundToOneDecimal((weightValue.value + amount).coerceAtLeast(0.0))
        weightValue.value = newValue
        val weightLbs = newValue.displayToLbs(weightUnit.value)
        // NonCancellable: tapping a spinner arrow and then immediately hitting
        // Back clears this ViewModel and cancels viewModelScope, which would
        // otherwise cut the write off before it reaches disk (this was the
        // cause of goal weight silently not saving).
        viewModelScope.launch(Dispatchers.IO) {
            withContext(NonCancellable) {
                when (field) {
                    ProfileWeightField.BEGINNING -> setBeginningWeight(lightContext.dataStore, weightLbs)
                    ProfileWeightField.GOAL -> setGoalWeight(lightContext.dataStore, weightLbs)
                }
            }
        }
    }

    private fun roundToOneDecimal(value: Double) = round(value * 10) / 10.0
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
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                WeightSpinnerRow(
                    weightValue = weightValue,
                    unitLabel = weightUnit.label,
                    onShiftWhole = viewModel::shiftWhole,
                    onShiftTenth = viewModel::shiftTenth,
                    modifier = Modifier.padding(top = 2f.gridUnitsAsDp()),
                )
            }
        }
    }
}
