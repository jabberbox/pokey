package com.thelightphone.sample

import com.thelightphone.sdk.SimpleLightScreen

fun SimpleLightScreen<*>.navigateToTab(tab: AppTab) {
    when (tab) {
        AppTab.HOME -> navigateTo(screenFactory = { HomeScreen(it) })
        AppTab.HISTORY -> navigateTo(screenFactory = { HistoryScreen(it) })
        AppTab.LOG -> navigateTo(screenFactory = { LogChoiceScreen(it) })
        AppTab.PROFILE -> navigateTo(screenFactory = { ProfileScreen(it) })
    }
}
