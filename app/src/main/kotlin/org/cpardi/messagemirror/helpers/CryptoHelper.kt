package org.cpardi.messagemirror.helpers

import android.util.Base64
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoHelper {
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private val CHARSET: Charset = Charsets.UTF_8
    private val secureRandom = SecureRandom()
    const val ALGORITHM = "AES"

    fun generateAESKey(keySize: Int = 256): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(keySize)
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: String, key: SecretKeySpec): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(16)
        secureRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encrypted = cipher.doFinal(plainText.toByteArray(CHARSET))
        // Prepend IV to encrypted bytes
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String, key: SecretKeySpec): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, 16)
        val encrypted = combined.copyOfRange(16, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, CHARSET)
    }
}
