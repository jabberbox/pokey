package com.thelightphone.sdk.shared

import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

object LightCrypto {

    /**
     * Decrypts an ECIES payload using the device-generated private key.
     *
     * Expected format (Base64-encoded):
     *   [2-byte big-endian ephemeral public key length]
     *   [ephemeral public key (X.509/SubjectPublicKeyInfo)]
     *   [12-byte IV]
     *   [AES-GCM ciphertext + 16-byte auth tag]
     */
    fun decrypt(privateKey: PrivateKey, encryptedBase64: String): String {
        val payload = Base64.decode(encryptedBase64)
        var offset = 0

        // Read ephemeral public key
        val ephemeralKeySize = ((payload[offset].toInt() and 0xFF) shl 8) or
                (payload[offset + 1].toInt() and 0xFF)
        offset += 2
        val ephemeralKeyBytes = payload.sliceArray(offset until offset + ephemeralKeySize)
        offset += ephemeralKeySize

        // Read IV
        val ivSize = 12
        val iv = payload.sliceArray(offset until offset + ivSize)
        offset += ivSize

        // Remaining is ciphertext + GCM tag
        val ciphertext = payload.sliceArray(offset until payload.size)

        // Reconstruct ephemeral public key
        val keyFactory = KeyFactory.getInstance("EC")
        val ephemeralPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(ephemeralKeyBytes))

        // ECDH key agreement
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(ephemeralPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Derive AES-256 key
        val aesKeyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(sharedSecret)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")

        // Decrypt with AES-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    fun encrypt(data: String, publicKeyBase64: String): String {
        val publicKeyBytes = Base64.decode(publicKeyBase64)
        val keyFactory = KeyFactory.getInstance("EC")
        val recipientPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyBytes))

        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        keyPairGenerator.initialize(ECGenParameterSpec("secp256r1"))
        val ephemeralKeyPair = keyPairGenerator.generateKeyPair()

        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(ephemeralKeyPair.private)
        keyAgreement.doPhase(recipientPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        val aesKeyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(sharedSecret)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")

        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        val ephemeralPublicKeyBytes = ephemeralKeyPair.public.encoded
        val ephemeralKeySize = ephemeralPublicKeyBytes.size

        val payload = ByteArrayOutputStream()
        payload.write((ephemeralKeySize shr 8) and 0xFF)
        payload.write(ephemeralKeySize and 0xFF)
        payload.write(ephemeralPublicKeyBytes)
        payload.write(iv)
        payload.write(ciphertext)

        return Base64.encode(payload.toByteArray())
    }
}