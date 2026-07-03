package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SealedLightContext
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.buildDatabase
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.round

internal val DISPLAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

internal fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

internal fun Long.toZonedDateTime(): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())

internal fun Long.toLocalTime(): LocalTime = toZonedDateTime().toLocalTime()

/** e.g. (2, 3) -> "2 days, 3 hours"; (0, 3) -> "3 hours"; (2, 0) -> "2 days" */
internal fun formatDaysHours(days: Long, hours: Long): String {
    val dayPart = if (days > 0) "$days day${if (days == 1L) "" else "s"}" else null
    val hourPart = if (hours > 0) "$hours hour${if (hours == 1L) "" else "s"}" else null
    return when {
        dayPart != null && hourPart != null -> "$dayPart, $hourPart"
        dayPart != null -> dayPart
        hourPart != null -> hourPart
        else -> "less than an hour"
    }
}

class HomeScreenViewModel(
    private val shotRepository: ShotRepository,
    private val weightRepository: WeightRepository,
    private val lightContext: SealedLightContext,
) : LightViewModel<Unit>() {

    val latestShot = MutableStateFlow<LoggedShot?>(null)
    val latestWeight = MutableStateFlow<LoggedWeight?>(null)
    val reminderFiredAt = MutableStateFlow<Long?>(null)
    val goalWeightLbs = MutableStateFlow<Double?>(null)
    val weightUnit = MutableStateFlow(WeightUnit.LBS)
    val timeFormat = MutableStateFlow(TimeFormat.HOUR_12)

    init {
        ReminderScheduler.ensureScheduled(lightContext)
        viewModelScope.launch {
            reminderFiredAtFlow(lightContext.dataStore).collect { reminderFiredAt.value = it }
        }
        viewModelScope.launch {
            profileFlow(lightContext.dataStore).collect { profile ->
                goalWeightLbs.value = profile.goalWeightLbs
                weightUnit.value = profile.weightUnit
                timeFormat.value = profile.timeFormat
            }
        }
    }

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        super.onScreenShow(screen)
        viewModelScope.launch(Dispatchers.IO) {
            latestShot.value = shotRepository.getLatestShot()
            latestWeight.value = weightRepository.getLatestWeight()
        }
    }

    fun dismissReminder() {
        viewModelScope.launch { clearReminderFired(lightContext.dataStore) }
    }
}

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) : LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {

    private val database = AppDatabase.getInstance {
        lightContext.buildDatabase(AppDatabase::class.java, AppDatabase.DATABASE_NAME)
    }
    private val shotRepository = ShotRepository.getInstance(database)
    private val weightRepository = WeightRepository.getInstance(database)

    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    override fun createViewModel(): HomeScreenViewModel {
        return HomeScreenViewModel(shotRepository, weightRepository, lightContext)
    }

    @Composable
    override fun Content() {
        val latestShot by viewModel.latestShot.collectAsState()
        val latestWeight by viewModel.latestWeight.collectAsState()
        val reminderFiredAt by viewModel.reminderFiredAt.collectAsState()
        val goalWeightLbs by viewModel.goalWeightLbs.collectAsState()
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
                    center = LightTopBarCenter.Text("Pokey"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (reminderFiredAt != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.dismissReminder() }
                                .padding(bottom = 1.5f.gridUnitsAsDp()),
                        ) {
                            LightText(
                                text = "Reminder: Shot day!",
                                variant = LightTextVariant.Subheading,
                                align = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            LightText(
                                text = "Tap to dismiss",
                                variant = LightTextVariant.Detail,
                                lighten = true,
                                align = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 0.25f.gridUnitsAsDp(), bottom = 0.5f.gridUnitsAsDp()),
                            )
                            Divider()
                        }
                    }

                    val shot = latestShot
                    if (shot == null) {
                        LightText(
                            text = "No shots logged yet.",
                            variant = LightTextVariant.Copy,
                            lighten = true,
                            modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                        )
                    } else {
                        val lastShotDateTime = shot.timestampMillis.toZonedDateTime()
                        val nextShotDateTime = lastShotDateTime.plusDays(7)
                        val now = ZonedDateTime.now()
                        val minutesUntil = ChronoUnit.MINUTES.between(now, nextShotDateTime)
                        val isOverdue = minutesUntil < 0
                        val absMinutes = abs(minutesUntil)
                        val days = absMinutes / (24 * 60)
                        val hours = (absMinutes % (24 * 60)) / 60

                        LightText(
                            text = when {
                                absMinutes == 0L -> "Next shot due now"
                                isOverdue -> "Next shot overdue by ${formatDaysHours(days, hours)}"
                                else -> "Next shot in ${formatDaysHours(days, hours)}"
                            },
                            variant = LightTextVariant.Subheading,
                            modifier = Modifier.padding(bottom = 0.75f.gridUnitsAsDp()),
                        )
                        LightText(
                            text = "Last shot: ${timeFormat.dateTimeFormatter().format(lastShotDateTime)}",
                            variant = LightTextVariant.Copy,
                            modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                        )
                    }

                    LightText(
                        text = "Current Weight",
                        variant = LightTextVariant.Detail,
                        modifier = Modifier.padding(bottom = 0.25f.gridUnitsAsDp()),
                    )
                    val weight = latestWeight
                    LightText(
                        text = if (weight == null) {
                            "No weight logged yet."
                        } else {
                            "${"%.1f".format(weight.weightLbs.lbsToDisplay(weightUnit))} ${weightUnit.label}"
                        },
                        variant = LightTextVariant.Copy,
                        lighten = true,
                    )

                    val goal = goalWeightLbs
                    if (weight != null && goal != null) {
                        val diff = round((weight.weightLbs - goal).lbsToDisplay(weightUnit) * 10) / 10.0
                        LightText(
                            text = when {
                                diff > 0.0 -> "${"%.1f".format(diff)} ${weightUnit.label} to goal"
                                diff < 0.0 -> "${"%.1f".format(-diff)} ${weightUnit.label} past goal"
                                else -> "At goal weight"
                            },
                            variant = LightTextVariant.Detail,
                            lighten = true,
                            modifier = Modifier.padding(top = 0.25f.gridUnitsAsDp()),
                        )
                    }
                }

                BottomNavBar(current = AppTab.HOME, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
