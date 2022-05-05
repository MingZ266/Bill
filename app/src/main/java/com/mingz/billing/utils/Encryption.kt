package com.mingz.billing.utils

import android.content.Context
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.reflect.KProperty

class Encryption(private val context: Context) : LocalEncryption() {

    companion object {
        private var key = ByteArray(0)

        private val dir by InitDir()
        private val passwordFile by lazy { File(dir, Tools.md5("password")) }
        private val protectionFile by lazy { File(dir, Tools.md5("protection")) }
        private val biometricsFile by lazy { File(dir, Tools.md5("biometrics")) }
        private val verify by InitVerify()

        @JvmStatic
        fun init(applicationContext: Context) {
            InitDir.init(applicationContext)
            InitVerify.init(applicationContext)
        }

        @JvmStatic
        fun encryptIfNeed(data: ByteArray): ByteArray {
            return if (key.isEmpty()) data else aesEncrypt(key, data)
        }

        @JvmStatic
        fun encryptIfNeed(data: String) = encryptIfNeed(data.toByteArray(StandardCharsets.UTF_8))

        @JvmStatic
        fun decryptIfNeed(ciphertext: ByteArray): ByteArray {
            return if (key.isEmpty()) ciphertext else aesDecrypt(key, ciphertext)
        }

        @JvmStatic
        fun decryptIfNeedAndToString(ciphertext: ByteArray) =
            String(decryptIfNeed(ciphertext), StandardCharsets.UTF_8)
    }

    override fun savePassword(password: ByteArray) {
        Tools.saveFile(passwordFile, password)
    }

    override fun readPassword(): ByteArray? {
        return Tools.readFile(passwordFile)
    }

    override fun saveProtection(protection: ByteArray) {
        Tools.saveFile(protectionFile, protection)
    }

    override fun readProtection(): ByteArray? {
        return Tools.readFile(protectionFile)
    }

    override fun saveBiometrics(biometrics: ByteArray) {
        Tools.saveFile(biometricsFile, biometrics)
    }

    override fun readBiometrics(): ByteArray? {
        return Tools.readFile(biometricsFile)
    }

    override fun verify() = verify

    override fun passwordError() = Tools.showToastOnUiThread(context, "密码错误")

    override fun biometricsError() = Tools.showToastOnUiThread(context, "验证失败")

    override fun protectionError() = Tools.showToastOnUiThread(context, "验证失败")

    private class InitDir {
        companion object {
            private lateinit var dir: File

            fun init(applicationContext: Context) {
                dir = File(applicationContext.filesDir, Constant.encryptionDir)
                if (!(dir.exists() || dir.mkdirs())) {
                    MyLog("Encryption").e("目录创建失败: ${dir.absolutePath}")
                }
            }
        }

        operator fun getValue(companion: Encryption.Companion, property: KProperty<*>) = dir
    }

    private class InitVerify {
        companion object {
            private lateinit var verify: ByteArray

            fun init(applicationContext: Context) {
                applicationContext.packageName.let {
                    MyLog.TEMP.v(it)
                    verify = it.toByteArray(StandardCharsets.UTF_8)
                }
            }
        }

        operator fun getValue(companion: Encryption.Companion, property: KProperty<*>) = verify
    }
}