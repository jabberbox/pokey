package com.thelightphone.sample

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val BEGINNING_WEIGHT_KEY = doublePreferencesKey("beginning_weight_lbs")
private val GOAL_WEIGHT_KEY = doublePreferencesKey("goal_weight_lbs")
private val WEIGHT_UNIT_KEY = stringPreferencesKey("weight_unit")
private val TIME_FORMAT_KEY = stringPreferencesKey("time_format")

data class Profile(
    val beginningWeightLbs: Double?,
    val goalWeightLbs: Double?,
    val weightUnit: WeightUnit,
    val timeFormat: TimeFormat,
)

fun profileFlow(dataStore: DataStore<Preferences>): Flow<Profile> =
    dataStore.data.map { prefs ->
        Profile(
            beginningWeightLbs = prefs[BEGINNING_WEIGHT_KEY],
            goalWeightLbs = prefs[GOAL_WEIGHT_KEY],
            weightUnit = prefs.toWeightUnit(),
            timeFormat = prefs.toTimeFormat(),
        )
    }

/** Lightweight flow for screens that only need the unit, not the rest of the profile. */
fun weightUnitFlow(dataStore: DataStore<Preferences>): Flow<WeightUnit> =
    dataStore.data.map { it.toWeightUnit() }

/** Lightweight flow for screens that only need the time format, not the rest of the profile. */
fun timeFormatFlow(dataStore: DataStore<Preferences>): Flow<TimeFormat> =
    dataStore.data.map { it.toTimeFormat() }

private fun Preferences.toWeightUnit(): WeightUnit =
    this[WEIGHT_UNIT_KEY]?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() } ?: WeightUnit.LBS

private fun Preferences.toTimeFormat(): TimeFormat =
    this[TIME_FORMAT_KEY]?.let { runCatching { TimeFormat.valueOf(it) }.getOrNull() } ?: TimeFormat.HOUR_12

suspend fun setBeginningWeight(dataStore: DataStore<Preferences>, weightLbs: Double?) {
    dataStore.edit { prefs ->
        if (weightLbs == null) prefs.remove(BEGINNING_WEIGHT_KEY) else prefs[BEGINNING_WEIGHT_KEY] = weightLbs
    }
}

suspend fun setGoalWeight(dataStore: DataStore<Preferences>, weightLbs: Double?) {
    dataStore.edit { prefs ->
        if (weightLbs == null) prefs.remove(GOAL_WEIGHT_KEY) else prefs[GOAL_WEIGHT_KEY] = weightLbs
    }
}

suspend fun setWeightUnit(dataStore: DataStore<Preferences>, unit: WeightUnit) {
    dataStore.edit { prefs -> prefs[WEIGHT_UNIT_KEY] = unit.name }
}

suspend fun setTimeFormat(dataStore: DataStore<Preferences>, format: TimeFormat) {
    dataStore.edit { prefs -> prefs[TIME_FORMAT_KEY] = format.name }
}
