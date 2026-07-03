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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SealedLightContext
import com.thelightphone.sdk.buildDatabase
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.round

private const val DEFAULT_WEIGHT_LBS = 150.0

enum class LogWeightField(val label: String) {
    DATE("Date"),
    TIME("Time"),
    WEIGHT("Weight"),
}

class LogWeightViewModel(
    private val repository: WeightRepository,
    private val lightContext: SealedLightContext,
    private val editingWeightId: Long?,
) : LightViewModel<Unit>() {

    val selectedDate = MutableStateFlow(LocalDate.now())
    val selectedTime = MutableStateFlow(LocalTime.now())
    val weightValue = MutableStateFlow(DEFAULT_WEIGHT_LBS)
    val weightUnit = MutableStateFlow(WeightUnit.LBS)
    val selectedField = MutableStateFlow(LogWeightField.DATE)
    val deleteArmed = MutableStateFlow(false)
    val timeFormat = MutableStateFlow(TimeFormat.HOUR_12)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val unit = weightUnitFlow(lightContext.dataStore).first()
            weightUnit.value = unit
            timeFormat.value = timeFormatFlow(lightContext.dataStore).first()
            if (editingWeightId != null) {
                repository.getWeightById(editingWeightId)?.let { weight ->
                    selectedDate.value = weight.timestampMillis.toLocalDate()
                    selectedTime.value = weight.timestampMillis.toLocalTime()
                    weightValue.value = weight.weightLbs.lbsToDisplay(unit)
                }
            } else {
                val lastWeightLbs = repository.getLatestWeight()?.weightLbs ?: DEFAULT_WEIGHT_LBS
                weightValue.value = lastWeightLbs.lbsToDisplay(unit)
            }
        }
    }

    fun selectField(field: LogWeightField) {
        selectedField.value = field
        deleteArmed.value = false
    }

    fun shiftMonth(months: Long) {
        selectedDate.value = selectedDate.value.plusMonths(months)
    }

    fun shiftDay(days: Long) {
        selectedDate.value = selectedDate.value.plusDays(days)
    }

    fun shiftHour(hours: Long) {
        selectedTime.value = selectedTime.value.plusHours(hours)
    }

    fun shiftMinute(minutes: Long) {
        selectedTime.value = selectedTime.value.plusMinutes(minutes)
    }

    fun shiftYear(years: Long) {
        selectedDate.value = selectedDate.value.plusYears(years)
    }

    fun toggleAmPm() {
        selectedTime.value = selectedTime.value.plusHours(12)
    }

    fun shiftWeightWhole(amount: Int) {
        weightValue.value = roundToOneDecimal((weightValue.value + amount).coerceAtLeast(0.0))
    }

    fun shiftWeightTenth(tenths: Int) {
        weightValue.value = roundToOneDecimal((weightValue.value + tenths / 10.0).coerceAtLeast(0.0))
    }

    private fun roundToOneDecimal(value: Double) = round(value * 10) / 10.0

    fun armDelete() {
        deleteArmed.value = true
    }

    fun submit(onLogged: () -> Unit) {
        val timestampMillis = selectedDate.value
            .atTime(selectedTime.value)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val weightLbs = weightValue.value.displayToLbs(weightUnit.value)
        viewModelScope.launch(Dispatchers.IO) {
            if (editingWeightId != null) {
                repository.updateWeight(editingWeightId, timestampMillis, weightLbs)
            } else {
                repository.logWeight(timestampMillis, weightLbs)
            }
            withContext(Dispatchers.Main) {
                onLogged()
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = editingWeightId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWeight(id)
            withContext(Dispatchers.Main) {
                onDeleted()
            }
        }
    }
}

class LogWeightScreen(
    sealedActivity: SealedLightActivity,
    private val editingWeightId: Long? = null,
) : LightScreen<Unit, LogWeightViewModel>(sealedActivity) {

    private val database = AppDatabase.getInstance {
        lightContext.buildDatabase(AppDatabase::class.java, AppDatabase.DATABASE_NAME)
    }
    private val repository = WeightRepository.getInstance(database)

    override val viewModelClass: Class<LogWeightViewModel>
        get() = LogWeightViewModel::class.java

    override fun createViewModel() = LogWeightViewModel(repository, lightContext, editingWeightId)

    @Composable
    override fun Content() {
        val selectedDate by viewModel.selectedDate.collectAsState()
        val selectedTime by viewModel.selectedTime.collectAsState()
        val weightValue by viewModel.weightValue.collectAsState()
        val weightUnit by viewModel.weightUnit.collectAsState()
        val selectedField by viewModel.selectedField.collectAsState()
        val deleteArmed by viewModel.deleteArmed.collectAsState()
        val timeFormat by viewModel.timeFormat.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text(if (editingWeightId != null) "Edit Weight" else "Log Weight"),
                    rightButton = LightBarButton.Text(
                        text = "SAVE",
                        onClick = { viewModel.submit(onLogged = { goBack() }) },
                    ),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                ) {
                    FieldSelectorRow(
                        fields = LogWeightField.entries,
                        selected = selectedField,
                        label = { it.label },
                        value = { field ->
                            when (field) {
                                LogWeightField.DATE -> DISPLAY_DATE_FORMAT.format(selectedDate)
                                LogWeightField.TIME -> timeFormat.timeFormatter().format(selectedTime)
                                LogWeightField.WEIGHT -> "%.1f ${weightUnit.label}".format(weightValue)
                            }
                        },
                        onSelect = viewModel::selectField,
                        modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                    )

                    when (selectedField) {
                        LogWeightField.DATE -> DateSpinnerRow(
                            date = selectedDate,
                            onShiftMonth = viewModel::shiftMonth,
                            onShiftDay = viewModel::shiftDay,
                            onShiftYear = viewModel::shiftYear,
                        )

                        LogWeightField.TIME -> TimeSpinnerRow(
                            time = selectedTime,
                            use24Hour = timeFormat == TimeFormat.HOUR_24,
                            onShiftHour = viewModel::shiftHour,
                            onShiftMinute = viewModel::shiftMinute,
                            onToggleAmPm = viewModel::toggleAmPm,
                        )

                        LogWeightField.WEIGHT -> WeightSpinnerRow(
                            weightValue = weightValue,
                            unitLabel = weightUnit.label,
                            onShiftWhole = viewModel::shiftWeightWhole,
                            onShiftTenth = viewModel::shiftWeightTenth,
                        )
                    }

                    if (editingWeightId != null) {
                        LightText(
                            text = if (deleteArmed) "Tap again to delete" else "Delete entry",
                            variant = LightTextVariant.Detail,
                            align = TextAlign.Center,
                            lighten = !deleteArmed,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (deleteArmed) {
                                        viewModel.delete(onDeleted = { goBack() })
                                    } else {
                                        viewModel.armDelete()
                                    }
                                }
                                .padding(top = 2f.gridUnitsAsDp()),
                        )
                    }
                }

                BottomNavBar(current = AppTab.LOG, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
