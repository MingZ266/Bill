package com.mingz.security.option

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.mingz.security.*
import com.mingz.security.databinding.ActivityFingerprintBinding
import com.mingz.share.*
import kotlinx.coroutines.*
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.PrivateKey
import java.util.*
import javax.crypto.SecretKey
import javax.security.auth.x500.X500Principal

/**
 * 指纹识别安全项.
 *
 * 需至少存在其它安全项（密码、图案）中的一种才能使用.
 */
class FingerprintActivity : AppCompatActivity(), CoroutineScope, SafetyOption {
    override val coroutineContext = Dispatchers.Main

    private val myLog by lazy(LazyThreadSafetyMode.NONE) { MyLog("指纹安全项") }
    private val activity = this
    private val loading = Loading(activity) // 等待弹窗
    private lateinit var binding: ActivityFingerprintBinding
    private lateinit var config: Config

    companion object {
        // AndroidKeyStore
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"

        // 密钥条目在AndroidKeyStore中的别名
        private const val ALIAS_FINGERPRINT = CFG_FINGERPRINT_BOOL

        // 更新安全密钥密文
        @Suppress("unused") // 通过反射调用
        @JvmStatic
        fun whenSafeKeyUpdated(context: Context, safeKey: SecretKey) {
            // 查找密钥对，若为null则可能未设定该安全项
            val keyPair = findKeyPair() ?: return
            // 保存新的安全密钥的密文
            val ciphertext = RSA.encrypt(keyPair.public, safeKey.encoded)
            val file = safeKeyFile(context, FILE_KEY_FINGERPRINT)
            if (file.exists() || file.createNewFile()) {
                FileOutputStream(file).use { it.write(ciphertext) }
            }
        }

        /**
         * 从AndroidKeyStore中查找用于加密安全密钥的密钥对.
         */
        internal fun findKeyPair(): KeyPair? {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val entry = keyStore.getEntry(ALIAS_FINGERPRINT, null)
            if (entry is PrivateKeyEntry) { // 获取密钥对
                val keyPair = KeyPair(entry.certificate.publicKey, entry.privateKey)
                MyLog("查找密钥对").d("获取密钥对: 公钥(${keyPair.public.encoded?.size}) - 私钥(" +
                        "${keyPair.private.encoded?.size})")
                return keyPair
            }
            return null
        }

        /**
         * 使用[biometricPrompt]调用[BiometricPrompt.authenticate]发起指纹验证请求.
         */
        internal fun authenticate(biometricPrompt: BiometricPrompt, title: String, privateKey: PrivateKey) {
            biometricPrompt.authenticate(BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setNegativeButtonText("取消")
                .build(), BiometricPrompt.CryptoObject(RSA.getCipher(privateKey)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFingerprintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化配置文件
        config = Config(activity, FILE_CONFIG_SAFETY)
        // 初始化视图
        binding.enable.enable.initChecked(config[CFG_FINGERPRINT_BOOL, false])
        // 初始化监听
        with(binding.enable.enable) {
            setOnClickListener { if (isChecked) disable() else enable() }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    // 验证指纹后启用指纹
    private fun enable() {
        if (!config[CFG_PASSWORD_BOOL, false] && !config[CFG_PATTERN_BOOL, false]) {
            activity.showToast("请先设置密码或图案")
            return
        }
        // 启用指纹时重新生成密钥对
        val keyPair = generateKeyPair()
        verifyFingerprint(keyPair, "验证以启用指纹", object : VerifyCallback {
            override fun onSuccess() {
                launch {
                    loading.show("正在启用")
                    catchException({
                        // 生成新的安全密钥并更新
                        SafeKey.update(activity, generateSafeKey())
                        // 写配置：指纹已启用
                        config[CFG_FINGERPRINT_BOOL] = true
                        // 反馈已启用
                        binding.enable.enable.isChecked = true
                    }, {
                        activity.showToast("启用失败")
                        myLog.w("指纹启用失败", it, true)
                    })
                    loading.dismiss()
                }
            }
        })
    }

    // 验证指纹后禁用指纹
    private fun disable() {
        val keyPair = findKeyPair() ?: generateKeyPair()
        verifyFingerprint(keyPair, "验证以禁用指纹", object : VerifyCallback {
            override fun onSuccess() {
                launch {
                    loading.show("正在禁用")
                    catchException(myLog) {
                        // 写配置：指纹已禁用
                        config[CFG_FINGERPRINT_BOOL] = false
                        // 反馈已禁用
                        binding.enable.enable.isChecked = false
                        // 删除安全密钥密文
                        safeKeyFile(activity, FILE_KEY_FINGERPRINT).delete()
                        // 删除密钥对
                        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
                        keyStore.load(null)
                        keyStore.deleteEntry(ALIAS_FINGERPRINT)
                    }
                    loading.dismiss()
                }
            }
        })
    }

    // 验证指纹
    private fun verifyFingerprint(keyPair: KeyPair, title: String, callback: VerifyCallback) {
        // 通过校验私钥是否已解锁，以确认用户是否通过了生物识别验证
        val verify = RSA.encrypt(keyPair.public, ByteArray(RSA.MAX_ENCRYPT))
        authenticate(BiometricPrompt(activity, object : AuthenticationCallback(activity) {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                catchException(myLog) {
                    val cipher = result.cryptoObject?.cipher ?: return
                    // 尝试使用私钥，若不可使用（未通过身份验证）将抛出异常
                    RSA.decrypt(cipher, verify)
                    // 私钥可用（验证通过）
                    callback.onSuccess()
                }
            }
        }), title, keyPair.private)
    }

    // 从AndroidKeyStore中生成密钥对
    @Suppress("DEPRECATION")
    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator: KeyPairGenerator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 限制私钥只允许解密
            val specBuilder = KeyGenParameterSpec.Builder(ALIAS_FINGERPRINT, KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(RSA.KEY_SIZE)
                // 限制只允许使用ECB模式
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                // 限制加密时只允许使用PKCS1填充方案
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                // 限制私钥只允许在通过身份验证后使用
                .setUserAuthenticationRequired(true)
            // 限制私钥只能在每次身份验证后使用
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                specBuilder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            } else {
                specBuilder.setUserAuthenticationValidityDurationSeconds(-1)
            }
            keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE).apply {
                initialize(specBuilder.build())
            }
        } else {
            // 证书生效起始时间（设为当前时间）
            val start = Calendar.getInstance()
            // 证书失效时间（设为当前时间 + 100年）
            val end = Calendar.getInstance().apply { add(Calendar.YEAR, 100) }
            keyPairGenerator = KeyPairGenerator.getInstance(RSA.ALGORITHM, ANDROID_KEY_STORE).apply {
                initialize(KeyPairGeneratorSpec.Builder(activity)
                    .setAlias(ALIAS_FINGERPRINT)
                    .setKeySize(RSA.KEY_SIZE)
                    .setSubject(X500Principal("C=CN"))
                    .setSerialNumber(BigInteger.valueOf(start.timeInMillis))
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build())
            }
        }
        return keyPairGenerator.generateKeyPair().apply {
            myLog.d("生成密钥对: 公钥(${public.encoded?.size}) - 私钥(${private.encoded?.size})")
        }
    }

    /**
     * 生物识别验证回调.
     */
    open class AuthenticationCallback(private val context: Context) : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationFailed() {
            context.showToast("识别失败")
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            MyLog().d("$errorCode: $errString")
            if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                && errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                context.showToast(errString.toString())
            }
        }
    }

    // 指纹验证结果回调
    private interface VerifyCallback {
        /**
         * 当指纹验证成功时回调.
         */
        fun onSuccess()
    }
}