package com.mingz.data

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.crypto.SecretKey
import kotlin.reflect.KProperty

// 安全密钥存储文件名称
private const val FILE_SAFE_KEY = "db_data.dat"

/**
 * 安全密钥.
 *
 * 同时也是密钥数据库和基础数据集的加解密密钥.
 */
internal val safeKey by InitSafeKey()

// 安全密钥读写文件
private val safeKeyFile = InternalFilePack(FILE_SAFE_KEY)

/**
 * 存储安全密钥密文.
 * @see readSafeKeyCiphertext
 */
fun writeSafeKeyCiphertext(ciphertext: ByteArray) {
    val file = safeKeyFile.file
    if (file.exists() || file.createNewFile()) {
        try {
            FileOutputStream(file).use { it.write(ciphertext) }
        } catch (e: IOException) {
            // TODO: log
        }
    }
}

/**
 * 读取安全密钥存储密文.
 * @see writeSafeKeyCiphertext
 */
fun readSafeKeyCiphertext(applicationContext: Context) = try {
    safeKeyFile.init(applicationContext)
    FileInputStream(safeKeyFile.file).use { it.readBytes() }
} catch (e: FileNotFoundException) {
    // 视为首次使用软件（一般情况下确实如此）
    null
} catch (e: IOException) {
    // TODO: log
    null
}

/**
 * 用于初始化存储在内部存储files目录下的文件.
 * @param childPath 在内部存储files目录下的路径，其中包含的目录将会被创建（不保证创建成功），但不会创建文件.
 */
internal class InternalFilePack(private val childPath: String) {
    lateinit var file: File
        private set
    private var noInit = true

    /**
     * 初始化文件.
     */
    fun init(applicationContext: Context) {
        if (noInit) {
            noInit = false
            file = File(applicationContext.filesDir, childPath).apply {
                parentFile?.let { if (!it.exists()) it.mkdirs() }
            }
        }
    }
}

// TODO: 在“安全访问”模块验证成功后调用[InitSafeKey.init]
// 代理类
class InitSafeKey {
    companion object {
        private lateinit var key: SecretKey
        private var noInit = true

        /**
         * 在“安全访问”模块内调用以设置软件运行时使用的安全密钥.
         */
        @JvmStatic
        fun init(safeKey: SecretKey) {
            if (noInit) {
                noInit = false
                key = safeKey
            }
        }
    }

    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = key
}
