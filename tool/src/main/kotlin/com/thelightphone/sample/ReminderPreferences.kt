package com.thelightphone.sample

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val REMINDER_FIRED_AT_KEY = longPreferencesKey("reminder_fired_at")

fun reminderFiredAtFlow(dataStore: DataStore<Preferences>): Flow<Long?> =
    dataStore.data.map { it[REMINDER_FIRED_AT_KEY] }

suspend fun markReminderFired(dataStore: DataStore<Preferences>, atMillis: Long) {
    dataStore.edit { it[REMINDER_FIRED_AT_KEY] = atMillis }
}

suspend fun clearReminderFired(dataStore: DataStore<Preferences>) {
    dataStore.edit { it.remove(REMINDER_FIRED_AT_KEY) }
}
