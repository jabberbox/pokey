package com.thelightphone.sample

import com.thelightphone.sdk.ui.LightIconConfiguration
import com.thelightphone.sdk.ui.LightIcons

enum class AppTab(val icon: LightIconConfiguration?) {
    HOME(null), // rendered with the custom HouseIcon instead — see BottomNavBar
    HISTORY(LightIcons.LARGE_LIST),
    LOG(LightIcons.ADD),
    PROFILE(LightIcons.CONTACTS),
}
