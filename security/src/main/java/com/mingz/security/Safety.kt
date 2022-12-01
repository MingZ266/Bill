package com.mingz.security

import android.content.Context
import android.security.keystore.KeyProperties
import com.mingz.data.SafeKeyProvider
import com.mingz.data.setSafeKeyProvider
import com.mingz.data.whenSafeKeyUpdated
import com.mingz.security.option.FingerprintActivity
import com.mingz.security.option.PasswordActivity
import com.mingz.security.option.PatternActivity
import com.mingz.security.option.SafetyOption
import com.mingz.share.*
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 用于更新安全项保存的安全密钥密文的静态方法名.
 */
private const val SAFETY_OPTION_FUN_NAME = "whenSafeKeyUpdated"

// 配置文件参数
/**
 * 是否设置了密码.
 */
internal const val CFG_PASSWORD_BOOL = "password"

/**
 * 是否设置了图案.
 */
internal const val CFG_PATTERN_BOOL = "pattern"

/**
 * 是否设置了指纹.
 */
internal const val CFG_FINGERPRINT_BOOL = "fingerprint"

// 存储文件名（均位于内部存储files目录下）
/**
 * 未设置安全项时安全密钥存储文件.
 */
private const val FILE_KEY_NONE = "data_1.dat"

/**
 * 密码安全项加密安全密钥后的密文存储文件.
 */
internal const val FILE_KEY_PASSWORD = "data_2.dat"

/**
 * 图案安全项加密安全密钥后的密文存储文件.
 */
internal const val FILE_KEY_PATTERN = "data_3.dat"

/**
 * 指纹安全项加密安全密钥后的密文存储文件.
 */
internal const val FILE_KEY_FINGERPRINT = "data_4.dat"

//******当增减安全项时需注意修改******
/**
 * 所有安全项.
 *
 * 新增安全项需在此注册.
 */
private val safetyOptionSet: Array<Class<out SafetyOption>>
    get() = arrayOf(
        PasswordActivity::class.java,
        PatternActivity::class.java,
        FingerprintActivity::class.java
    )

/**
 * 检查是否允许禁用[goal]对应的安全项.
 * @param goal 取值[CFG_PASSWORD_BOOL]或[CFG_PATTERN_BOOL]
 * @return 当指纹已启用且禁用[goal]对应的安全项后将只剩指纹安全项时返回true
 */
internal fun notAllowedToDisable(config: Config, goal: String): Boolean {
    if (config[CFG_FINGERPRINT_BOOL, false]) { // 指纹安全项已启用
        val password = if (goal == CFG_PASSWORD_BOOL) false else config[CFG_PASSWORD_BOOL, false]
        val pattern = if (goal == CFG_PATTERN_BOOL) false else config[CFG_PATTERN_BOOL, false]
        return !(password || pattern)
    }
    return false
}
//******************************

/**
 * 获取安全密钥的存储文件.
 * @param name [FILE_KEY_NONE]、[FILE_KEY_PASSWORD]、[FILE_KEY_PATTERN]、[FILE_KEY_FINGERPRINT]之一
 */
internal fun safeKeyFile(context: Context, name: String) = quoteFile(context, true, name)

/**
 * 检查是否需要验证安全项.
 *
 * 若不需要将读取或生成安全密钥；若需要，应当转到[SafetyVerifyActivity]以验证安全项.
 * @return true表示需要验证安全项，否则不需要.
 */
suspend fun checkSafetyOption(context: Context): Boolean {
    setSafeKeyProvider(SafeKey.instance)
    val config = Config(context, FILE_CONFIG_SAFETY)
    return (config[CFG_PASSWORD_BOOL, false] || config[CFG_PATTERN_BOOL, false] ||
            config[CFG_FINGERPRINT_BOOL, false]).onFalse {
        val myLog = MyLog("Safety")
        catchException(myLog) {
            // 存储的安全密钥未加密，直接读取作为安全密钥
            val safeKeyFile = safeKeyFile(context, FILE_KEY_NONE)
            if (safeKeyFile.exists()) {
                SafeKey.init(AES.generateKey(readFile(safeKeyFile)))
                myLog.i("直接读取安全密钥")
            } else { // 创建安全密钥
                val safeKey = generateSafeKey()
                SafeKey.init(safeKey)
                myLog.v("保存安全密钥")
                // 保存安全密钥
                if (!saveFile(safeKeyFile, safeKey.encoded)) {
                    myLog.w("安全密钥保存失败: 文件创建失败（${safeKeyFile.absolutePath}）")
                }
            }
        }
    }
}

/**
 * 生成安全密钥.
 */
internal fun generateSafeKey() = AES.generateKey()

/**
 * 将多个字节数组合并为一个字节数组.
 *
 * 合并后数据内容为：字节数组1的长度（4字节）、字节数组1、字节数组2的长度、字节数组2、...、字节数组n的长度、字节数组n
 * @see split
 */
internal fun combine(vararg data: ByteArray): ByteArray {
    val n = data.size
    if (n < 2) throw IllegalArgumentException("合并只对两组及以上的数据有效")
    return ByteArrayOutputStream().use {
        for (d in data) {
            // 写入数组长度（低字节写入低地址）
            val size = d.size
            for (i in 0 until Int.SIZE_BYTES) {
                it.write(size ushr (8 * i))
            }
            // 写入数组
            it.write(d)
        }
        // 返回合并结果
        it.toByteArray()
    }
}

/**
 * 将一个由[combine]产生的字节数组拆分还原为多个字节数组.
 * @see combine
 * @throws IllegalArgumentException 如果[data]不是有效的可拆分对象
 */
internal fun split(data: ByteArray): Array<ByteArray> {
    val result = ArrayList<ByteArray>()
    ByteArrayInputStream(data).use {
        while (true) {
            // 读取数组长度（低地址读入低字节）
            var size = 0
            for (i in 0 until Int.SIZE_BYTES) {
                val b = it.read()
                if (b == -1) {
                    if (i == 0) return@use // 数据已拆分完毕
                    throw IllegalArgumentException("数组长度读取失败")
                }
                size = (b shl (8 * i)) or size
            }
            // 读取数组
            if (size < 0) {
                throw IllegalArgumentException("数组长度读取错误")
            }
            val d = ByteArray(size)
            if (it.read(d) == -1) {
                throw IllegalArgumentException("数组读取失败")
            }
            result.add(d)
        }
    }
    return result.toArray(emptyArray<ByteArray>())
}

/**
 * 对[data]生成SHA-256摘要.
 */
internal fun sha256(data: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(data)

/**
 * 安全密钥初始化及更新.
 */
internal class SafeKey private constructor(): SafeKeyProvider {
    companion object {
        /**
         * 安全密钥提供程序实例.
         */
        val instance = SafeKey()

        private lateinit var key: SecretKey

        /**
         * 初始化安全密钥.
         */
        fun init(safeKey: SecretKey) {
            key = safeKey
        }

        /**
         * 更新安全密钥.
         */
        suspend fun update(context: Context, safeKey: SecretKey) {
            val myLog = MyLog("Safety")
            key = safeKey // 更新缓存中的安全密钥
            val set = safetyOptionSet // 获取所有注册的安全项
            val tasks = ArrayList<Job>(1 + set.size) // io任务集
            // 更新存储数据（基础数据集+账单数据）的密文
            tasks.add(GlobalScope.launch { whenSafeKeyUpdated() })
            // 更新各安全项存储的安全密钥密文
            for (option in set) {
                tasks.add(GlobalScope.launch(Dispatchers.IO) {
                    catchException({
                        // 通过反射调用静态方法“whenSafeKeyUpdated(Context, SecretKey)”
                        option.getMethod(SAFETY_OPTION_FUN_NAME, Context::class.java, SecretKey::class.java)
                            .invoke(null, context, safeKey)
                    }, {
                        myLog.w("安全项“${option.simpleName}”更新安全密钥失败", it, true)
                    })
                })
            }
            // 等待io任务完成
            for (task in tasks) {
                task.join()
            }
            // 删除未加密的旧的安全密钥
            quoteFile(context, true, FILE_KEY_NONE).delete()
        }
    }

    override val safeKey: ByteArray
        get() = key.encoded

    override fun encrypt(data: ByteArray) = AES.encrypt(key, data)

    override fun decrypt(ciphertext: ByteArray) = AES.decrypt(key, ciphertext)
}

/**
 * 生成RSA密钥对，及使用[RSA.FULL_NAME]加解密.
 */
internal class RSA {
    companion object {
        /**
         * RSA算法名称.
         * @see KeyProperties.KEY_ALGORITHM_RSA
         */
        const val ALGORITHM = "RSA"

        /**
         * RSA密钥尺寸.
         */
        const val KEY_SIZE = 1024

        /**
         * RSA算法、模式及填充.
         * @see KeyProperties.BLOCK_MODE_ECB
         * @see KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
         */
        private const val FULL_NAME = "$ALGORITHM/ECB/PKCS1Padding"

        /**
         * RSA最大加密字节数.
         */
        const val MAX_ENCRYPT = KEY_SIZE / 8 - 11 // PKCS1填充11字节

        /**
         * RSA最大解密字节数.
         */
        private const val MAX_DECRYPT = KEY_SIZE / 8

        /**
         * 生成密钥对.
         */
        @JvmStatic
        fun generateKeyPair(): KeyPair {
            val generator = KeyPairGenerator.getInstance(ALGORITHM)
            generator.initialize(KEY_SIZE)
            return generator.generateKeyPair()
        }

        /**
         * 从[publicKey]中构造公钥.
         */
        @JvmStatic
        fun generatePublic(publicKey: ByteArray): PublicKey =
            KeyFactory.getInstance(ALGORITHM).generatePublic(X509EncodedKeySpec(publicKey))

        /**
         * 从[privateKey]中构造私钥.
         */
        @JvmStatic
        fun generatePrivate(privateKey: ByteArray): PrivateKey =
            KeyFactory.getInstance(ALGORITHM).generatePrivate(PKCS8EncodedKeySpec(privateKey))

        /**
         * 应用[FULL_NAME]，使用[publicKey]对[data]加密.
         */
        @JvmStatic
        fun encrypt(publicKey: PublicKey, data: ByteArray) =
            rsaOperate(data, Cipher.getInstance(FULL_NAME).apply {
                init(Cipher.ENCRYPT_MODE, publicKey)
            }, MAX_ENCRYPT)

        /**
         * 应用[FULL_NAME]，使用[privateKey]对[ciphertext]解密.
         */
        @JvmStatic
        fun decrypt(privateKey: PrivateKey, ciphertext: ByteArray) =
            rsaOperate(ciphertext, getCipher(privateKey), MAX_DECRYPT)

        /**
         * 对于应用[FULL_NAME]并设置了[PrivateKey]的[cipher]，解密[ciphertext].
         */
        @JvmStatic
        fun decrypt(cipher: Cipher, ciphertext: ByteArray) = rsaOperate(ciphertext, cipher, MAX_DECRYPT)

        /**
         * 获取应用[FULL_NAME]并设置了[privateKey]的[Cipher].
         */
        @JvmStatic
        fun getCipher(privateKey: PrivateKey): Cipher =
            Cipher.getInstance(FULL_NAME).apply { init(Cipher.DECRYPT_MODE, privateKey) }

        /**
         * 使用RSA算法进行分组加解密操作.
         * @param src 待加密数据或待解密数据
         * @param cipher 已初始化过密钥的[FULL_NAME]实例对象
         * @param max 每组数据的最大长度，值为[MAX_ENCRYPT]或[MAX_DECRYPT]
         */
        private fun rsaOperate(src: ByteArray, cipher: Cipher, max: Int): ByteArray {
            val srcSize = src.size
            val amount = srcSize / max // 以max为字节数，可分多少组（不计余数）
            return ByteArrayOutputStream(srcSize).use {
                var start = 0
                for (i in 0 until amount) {
                    val end = start + max
                    it.write(cipher.doFinal(src.copyOfRange(start, end)))
                    start = end
                }
                // 以max字节数分组计算余数，若非0，需额外记一组
                val remainder = srcSize % max
                if (remainder != 0) {
                    it.write(cipher.doFinal(src.copyOfRange(start, start + remainder)))
                }
                it.toByteArray()
            }
        }
    }
}

/**
 * 生成AES密钥，及使用[AES.FULL_NAME]加解密.
 */
internal class AES {
    companion object {
        /**
         * AES算法名称.
         */
        private const val ALGORITHM = "AES"

        /**
         * AES密钥长度.
         */
        private const val KEY_SIZE = 256

        /**
         * AES加密模式及填充.
         *
         * 需要使用iv参数.
         */
        private const val FULL_NAME = "AES/CFB/NoPadding"

        /**
         * 生成密钥.
         */
        @JvmStatic
        fun generateKey(): SecretKey {
            val generator = KeyGenerator.getInstance(ALGORITHM)
            generator.init(KEY_SIZE)
            return generator.generateKey()
        }

        /**
         * 从[key]中构造密钥.
         */
        @JvmStatic
        fun generateKey(key: ByteArray): SecretKey = SecretKeySpec(key, ALGORITHM)

        /**
         * 应用[FULL_NAME]，使用[key]对[data]加密.
         * @return 以iv参数、密文顺序合并后的结果
         */
        @JvmStatic
        fun encrypt(key: SecretKey, data: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(FULL_NAME)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val ciphertext = cipher.doFinal(data)
            val iv = cipher.iv
            return combine(iv, ciphertext)
        }

        /**
         * 应用[FULL_NAME]，使用[key]对[ciphertext]解密.
         * @throws IllegalArgumentException 如果[ciphertext]不是以iv参数、密文顺序合并的结果
         */
        @JvmStatic
        fun decrypt(key: SecretKey, ciphertext: ByteArray): ByteArray {
            // 拆分
            val split = split(ciphertext)
            if (split.size < 2) {
                throw IllegalArgumentException("不是有效的密文")
            }
            val iv = split[0] // iv参数
            val text = split[1] // 密文
            // 解密
            val cipher = Cipher.getInstance(FULL_NAME)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            return cipher.doFinal(text)
        }
    }
}