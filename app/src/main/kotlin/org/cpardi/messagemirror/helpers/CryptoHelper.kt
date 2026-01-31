package org.cpardi.messagemirror.helpers

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
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

    fun isValidEncryptionKey(keyString: String?): Boolean {
        if (keyString.isNullOrEmpty()) return false

        return try {
            encrypt("test", keyString)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun encrypt(plainText: String, keyString: String): String {
        val keyBytes = Base64.decode(keyString, Base64.NO_WRAP)
        val key = SecretKeySpec(keyBytes, ALGORITHM)

        val compressed =
            ByteArrayOutputStream().use {
                GZIPOutputStream(it).use { gzip -> gzip.write(plainText.toByteArray(CHARSET)) }
                it.toByteArray()
            }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(size = 16)
        secureRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encrypted = cipher.doFinal(compressed)
        val combined = iv + encrypted // Prepend IV to encrypted bytes

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String, key: SecretKeySpec): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        val iv = combined.copyOfRange(fromIndex = 0, toIndex = 16)
        val encrypted = combined.copyOfRange(fromIndex = 16, toIndex = combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decrypted = cipher.doFinal(encrypted)
        val uncompressed = GZIPInputStream(decrypted.inputStream()).use { gzip -> gzip.readBytes() }

        return String(uncompressed, CHARSET)
    }
}
