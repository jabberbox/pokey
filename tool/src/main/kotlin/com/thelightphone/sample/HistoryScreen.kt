package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SealedLightContext
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.buildDatabase
import com.thelightphone.sdk.ui.LightIcon
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
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val shotRepository: ShotRepository,
    private val weightRepository: WeightRepository,
    private val lightContext: SealedLightContext,
) : LightViewModel<Unit>() {

    val items = MutableStateFlow<List<HistoryItem>>(emptyList())
    val weightUnit = MutableStateFlow(WeightUnit.LBS)
    val timeFormat = MutableStateFlow(TimeFormat.HOUR_12)

    init {
        viewModelScope.launch {
            weightUnitFlow(lightContext.dataStore).collect { weightUnit.value = it }
        }
        viewModelScope.launch {
            timeFormatFlow(lightContext.dataStore).collect { timeFormat.value = it }
        }
    }

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        super.onScreenShow(screen)
        viewModelScope.launch(Dispatchers.IO) {
            val shots = shotRepository.getAllShots()
                .map { HistoryItem.ShotItem(it.id, it.timestampMillis, it.site) }
            val weights = weightRepository.getAllWeights()
                .map { HistoryItem.WeightItem(it.id, it.timestampMillis, it.weightLbs) }
            items.value = (shots + weights).sortedByDescending { it.timestampMillis }
        }
    }
}

class HistoryScreen(
    sealedActivity: SealedLightActivity,
) : LightScreen<Unit, HistoryViewModel>(sealedActivity) {

    private val database = AppDatabase.getInstance {
        lightContext.buildDatabase(AppDatabase::class.java, AppDatabase.DATABASE_NAME)
    }
    private val shotRepository = ShotRepository.getInstance(database)
    private val weightRepository = WeightRepository.getInstance(database)

    override val viewModelClass: Class<HistoryViewModel>
        get() = HistoryViewModel::class.java

    override fun createViewModel() = HistoryViewModel(shotRepository, weightRepository, lightContext)

    @Composable
    override fun Content() {
        val items by viewModel.items.collectAsState()
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
                    center = LightTopBarCenter.Text("History"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LightText(
                            text = "No entries yet.",
                            variant = LightTextVariant.Copy,
                            align = TextAlign.Center,
                            lighten = true,
                            modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp()),
                        )
                    }
                } else {
                    LightScrollView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 1f.gridUnitsAsDp()),
                    ) {
                        items.forEachIndexed { index, item ->
                            val date = timeFormat.dateTimeFormatter().format(item.timestampMillis.toZonedDateTime())
                            val label = when (item) {
                                is HistoryItem.ShotItem -> "Shot: ${item.site.label}"
                                is HistoryItem.WeightItem ->
                                    "Weight: ${"%.1f".format(item.weightLbs.lbsToDisplay(weightUnit))} ${weightUnit.label}"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (item) {
                                            is HistoryItem.ShotItem -> navigateTo(
                                                screenFactory = { LogShotScreen(it, editingShotId = item.id) },
                                            )

                                            is HistoryItem.WeightItem -> navigateTo(
                                                screenFactory = { LogWeightScreen(it, editingWeightId = item.id) },
                                            )
                                        }
                                    },
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    LightText(
                                        text = date,
                                        variant = LightTextVariant.Detail,
                                        lighten = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 0.75f.gridUnitsAsDp()),
                                    )
                                    LightText(
                                        text = label,
                                        variant = LightTextVariant.Copy,
                                        lighten = item is HistoryItem.WeightItem,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 0.125f.gridUnitsAsDp(), bottom = 0.75f.gridUnitsAsDp()),
                                    )
                                }
                                LightIcon(
                                    icon = LightIcons.PENCIL,
                                    size = 1.5f,
                                    modifier = Modifier.padding(start = 0.5f.gridUnitsAsDp()),
                                )
                            }
                            if (index != items.lastIndex) {
                                Divider()
                            }
                        }
                    }
                }

                BottomNavBar(current = AppTab.HISTORY, onNavigate = { navigateToTab(it) })
            }
        }
    }
}
