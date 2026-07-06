package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import kotlin.math.round

// Bump in 0.1 steps while dialing in the right size for the arc + text block.
private const val HOME_CONTENT_SCALE = 1.2f

internal val DISPLAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

internal fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

internal fun Long.toZonedDateTime(): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())

internal fun Long.toLocalTime(): LocalTime = toZonedDateTime().toLocalTime()

/**
 * Countdown to a shot that isn't due yet: days while more than a day
 * remains, hours inside the final 24 hours (so the text stays short enough
 * to fit the arc, instead of a fixed "Less than a day" that overflowed it).
 * Partial hours round up so a handful of remaining minutes still reads as
 * "1 hour" rather than "0 hours".
 */
internal fun formatDueIn(minutesUntil: Long): String {
    val hours = minutesUntil / 60
    if (hours >= 24) {
        val days = hours / 24
        return "$days day${if (days == 1L) "" else "s"}"
    }
    val roundedHours = ((minutesUntil + 59) / 60).coerceAtLeast(1)
    return "$roundedHours hour${if (roundedHours == 1L) "" else "s"}"
}

/**
 * How long past due: hours for the first day overdue, days after that.
 * Expects [overdueMinutes] >= 60 -- anything under an hour overdue is its
 * own "Shot Due" state instead.
 */
internal fun formatOverdue(overdueMinutes: Long): String {
    val hours = overdueMinutes / 60
    if (hours >= 24) {
        val days = hours / 24
        return "$days day${if (days == 1L) "" else "s"}"
    }
    return "$hours hour${if (hours == 1L) "" else "s"}"
}

class HomeScreenViewModel(
    private val shotRepository: ShotRepository,
    private val weightRepository: WeightRepository,
    private val lightContext: SealedLightContext,
) : LightViewModel<Unit>() {

    val latestShot = MutableStateFlow<LoggedShot?>(null)
    val latestWeight = MutableStateFlow<LoggedWeight?>(null)
    val goalWeightLbs = MutableStateFlow<Double?>(null)
    val weightUnit = MutableStateFlow(WeightUnit.LBS)
    val timeFormat = MutableStateFlow(TimeFormat.HOUR_12)

    init {
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
                        .padding(horizontal = 1f.gridUnitsAsDp())
                        .scale(HOME_CONTENT_SCALE),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val shot = latestShot
                    if (shot == null) {
                        LightText(
                            text = "No shots logged yet.",
                            variant = LightTextVariant.Copy,
                            modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                        )
                    } else {
                        val lastShotDateTime = shot.timestampMillis.toZonedDateTime()
                        val nextShotDateTime = lastShotDateTime.plusDays(7)
                        val now = ZonedDateTime.now()
                        val minutesUntil = ChronoUnit.MINUTES.between(now, nextShotDateTime)
                        val overdueMinutes = -minutesUntil

                        val totalCycleMinutes = 7 * 24 * 60
                        val elapsedMinutes = (totalCycleMinutes - minutesUntil).coerceIn(0, totalCycleMinutes.toLong())
                        val progress = elapsedMinutes.toFloat() / totalCycleMinutes

                        NextDoseArc(
                            progress = progress,
                            primaryText = when {
                                minutesUntil > 0 -> formatDueIn(minutesUntil).replaceFirstChar { it.uppercase() }
                                overdueMinutes < 60 -> "Shot Due"
                                else -> "Overdue"
                            },
                            secondaryText = when {
                                minutesUntil > 0 -> "to next shot"
                                overdueMinutes < 60 -> ""
                                else -> "by ${formatOverdue(overdueMinutes)}"
                            },
                            tertiaryText = timeFormat.nextDoseFormatter().format(nextShotDateTime),
                            modifier = Modifier.padding(bottom = 1.5f.gridUnitsAsDp()),
                        )
                    }

                    LightText(
                        text = "Current Weight",
                        variant = LightTextVariant.Fine,
                        modifier = Modifier.padding(bottom = 0.25f.gridUnitsAsDp()),
                    )
                    val weight = latestWeight
                    LightText(
                        text = if (weight == null) {
                            "No weight logged yet."
                        } else {
                            "${"%.1f".format(weight.weightLbs.lbsToDisplay(weightUnit))} ${weightUnit.label}"
                        },
                        variant = LightTextVariant.Heading,
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
                            modifier = Modifier.padding(top = 1f.gridUnitsAsDp()),
                        )
                    }
                }

                BottomNavBar(current = AppTab.HOME, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
