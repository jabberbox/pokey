package com.thelightphone.sdk

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import java.io.File
import kotlin.text.clear

abstract class SimpleLightScreen(sealedActivity: SealedLightActivity) {
    internal val activity = sealedActivity.activity

    protected val dataStore: DataStore<Preferences> = activity.dataStore
    protected val filesDir: File = activity.filesDir
    protected val fileShare: LightFileShare by lazy { LightFileShare(activity) }

    @Composable
    abstract fun Content()

    open val showBackBar: Boolean = true

    open fun willShow() {}
    open fun willHide() {}
    open fun onAppPause() {}
    open fun onScreenDestroy() {}

    internal open fun notifyWillShow() {
        willShow()
    }

    internal open fun notifyWillHide() {
        willHide()
    }

    internal open fun notifyAppPause() {
        onAppPause()
    }

    internal open fun destroy() {
        onScreenDestroy()
    }

    fun navigateTo(screenFactory: (SealedLightActivity) -> SimpleLightScreen) {
        val screen = screenFactory(SealedLightActivity(activity))
        activity.navigateTo(screen)
    }

    open fun goBack() {
        activity.goBack()
    }
}

abstract class LightScreen<VM : LightViewModel>(
    sealedActivity: SealedLightActivity
) : SimpleLightScreen(sealedActivity), ViewModelStoreOwner {
    abstract val viewModelClass: Class<VM>
    abstract fun createViewModel(): VM

    override val viewModelStore: ViewModelStore = ViewModelStore()

    @Suppress("UNCHECKED_CAST")
    val viewModel: VM by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return createViewModel() as T
            }
        }
        ViewModelProvider(this, factory)[viewModelClass]
    }

    override fun destroy() {
        super.destroy()
        viewModelStore.clear()
    }

    override fun notifyWillShow() {
        super.notifyWillShow()
        viewModel.onScreenShow(this)
    }

    override fun notifyWillHide() {
        super.notifyWillHide()
        viewModel.onScreenHide(this)
    }

    override fun notifyAppPause() {
        super.notifyAppPause()
        viewModel.onAppPause()
    }

    override fun goBack() {
        if (!viewModel.onBackPressed()) {
            super.goBack()
        }
    }
}
