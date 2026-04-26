package com.thelightphone.sdk

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.thelightphone.sdk.shared.LightConstants
import com.thelightphone.sdk.shared.LightServerHandshake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class LightSdkApplication : Application() {

    companion object {
        private const val TAG = "LightSdkApplication"
        private const val RESULT_OK = 0

        private val _lightOSData = MutableStateFlow<String?>(null)
        val lightOsData: StateFlow<LightServerHandshake?> = _lightOSData.asStateFlow()
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        invokeEntryPoint()
        registerWithLightServer()
    }

    // Tool may have registered an initialization function, call it
    private fun invokeEntryPoint() {
        val entryPoint = LightSdkRegistry.entryPoint ?: return

        runCatching {
            entryPoint.onToolCreate(lightOsData, applicationScope)
        }.onFailure {
            Log.e(TAG, "Failed to invoke @EntryPoint", it)
        }
    }

    private fun registerWithLightServer() {
        val publicKey = LightClientCrypto.getPublicKeyBase64()

        val intent = Intent(LightConstants.ACTION_SDK_HANDSHAKE).apply {
            setPackage(BuildConfig.LIGHT_SERVER_PACKAGE)

            // a PendingIntent will be annotated with the sending package name (this tool)
            // by the system, this lets LightOS confidently know where the broadcast came from
            val identity = PendingIntent.getActivity(
                this@LightSdkApplication,
                0,
                Intent(),
                PendingIntent.FLAG_IMMUTABLE
            )
            putExtra("sender_identity", identity)
            putExtra("public_key", publicKey)
        }

        // Send registration broadcast to LightOS, expect encrypted response
        sendOrderedBroadcast(
            intent,
            null,
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    if (resultCode != RESULT_OK) {
                        Log.w(TAG, "Server responded with code $resultCode")
                        return
                    }

                    val encryptedResponse = resultData ?: return

                    val decrypted = runCatching { LightClientCrypto.decrypt(encryptedResponse) }
                        .onFailure { Log.e(TAG, "Failed to decrypt Server response", it) }
                        .getOrNull() ?: return

                    _lightOSData.value = decrypted
                }
            },
            Handler(Looper.getMainLooper()),
            -1,
            null,
            null
        )
    }
}
