package com.thelightphone.reddit

import androidx.datastore.preferences.core.stringPreferencesKey

internal object RedditPreferences {
    val BRAVE_API_KEY = stringPreferencesKey("brave_api_key")
}
