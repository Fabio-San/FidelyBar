package com.card.fidelybar.data

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Crittografia dei backup FidelyBar.
 *
 * Formato "FIDELY1": PBKDF2 + AES-256-GCM con password scelta dall'utente.
 * Layout del blob: `FIDELY1:` + Base64( salt | nonce | ciphertext ).
 * GCM garantisce confidenzialità E integrità: ogni modifica al file (tampering)
 * fa fallire il decrypt, che restituisce null anche se la password è corretta.
 */
object CardCrypto {

    private const val MAGIC = "FIDELY1:"
    private const val SALT_LEN = 16
    private const val NONCE_LEN = 12
    private const val TAG_BITS = 128
    private const val KEY_BITS = 256
    private const val ITERATIONS = 120_000
    const val MIN_PASSWORD_LEN = 4

    fun isEncrypted(input: String): Boolean = input.startsWith(MAGIC)

    fun isValidPassword(password: String): Boolean =
        password.length >= MIN_PASSWORD_LEN

    fun encrypt(plainText: String, password: String): String {
        require(isValidPassword(password)) { "Password troppo corta" }
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val nonce = ByteArray(NONCE_LEN).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return MAGIC + Base64.getEncoder().encodeToString(salt + nonce + ciphertext)
    }

    fun decrypt(armored: String, password: String): String? {
        if (!isEncrypted(armored)) return null
        return runCatching {
            val raw = Base64.getDecoder().decode(armored.removePrefix(MAGIC))
            if (raw.size <= SALT_LEN + NONCE_LEN) return@runCatching null
            val salt = raw.copyOfRange(0, SALT_LEN)
            val nonce = raw.copyOfRange(SALT_LEN, SALT_LEN + NONCE_LEN)
            val ciphertext = raw.copyOfRange(SALT_LEN + NONCE_LEN, raw.size)
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        }.getOrNull()
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
    }
}