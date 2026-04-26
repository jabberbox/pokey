package com.thelightphone.sdk

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.thelightphone.sdk.shared.LightCrypto
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec

internal object LightClientCrypto {

    private const val KEYSTORE_ALIAS = "com.thelightphone.sdk.eckey"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

    /**
     * Returns the Base64-encoded X.509 public key, generating a new key pair on first call.
     */
    fun getPublicKeyBase64(): String {
        ensureKeyPair()
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val publicKey = keyStore.getCertificate(KEYSTORE_ALIAS).publicKey
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String): String {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val privateKey = keyStore.getKey(KEYSTORE_ALIAS, null) as java.security.PrivateKey
        return LightCrypto.decrypt(privateKey, encryptedBase64)
    }

    private fun ensureKeyPair() {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) return

        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_AGREE_KEY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .build()

        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_PROVIDER).apply {
            initialize(spec)
            generateKeyPair()
        }
    }
}
