package com.mingz.security.verify

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import com.mingz.security.*
import com.mingz.security.databinding.FragmentFingerprintBinding
import com.mingz.security.option.FingerprintActivity
import com.mingz.share.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.KeyStore
import kotlin.coroutines.CoroutineContext

class FingerprintFragment : SafetyVerifyFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    override val mTag = TAG

    private lateinit var binding: FragmentFingerprintBinding

    companion object {
        const val TAG = CFG_FINGERPRINT_BOOL

        /**
         * 检查指纹安全项是否可用.
         *
         * 当系统指纹变更时，密钥对中的私钥将失效，此时指纹安全项不可用.
         *
         * 若不可用，将禁用指纹安全项.
         * @return 若可用，将返回true
         */
        internal fun isAvailable(context: Context): Boolean {
            // 若API版本低于23，则密钥对未使用身份验证保护，一般不会失效
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
            val keyPair = FingerprintActivity.findKeyPair() ?: return false
            return try {
                RSA.getCipher(keyPair.private)
                true
            } catch (e: KeyPermanentlyInvalidatedException) { // 私钥失效
                val myLog = MyLog()
                myLog.w("指纹安全项已失效", e)
                catchException(myLog) {
                    // 写配置：指纹已禁用
                    Config(context, FILE_CONFIG_SAFETY)[CFG_FINGERPRINT_BOOL] = false
                    // 删除指纹安全项保存的安全密钥密文
                    safeKeyFile(context, FILE_KEY_FINGERPRINT).delete()
                    // 删除密钥对
                    val keyStore = KeyStore.getInstance(FingerprintActivity.ANDROID_KEY_STORE)
                    keyStore.load(null)
                    keyStore.deleteEntry(FingerprintActivity.ALIAS_FINGERPRINT)
                }
                false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFingerprintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        binding.verify.setOnClickListener { decryptSafeKey() }
    }

    override fun onShow() {
        decryptSafeKey()
    }

    /**
     * 请求验证指纹以解密安全密钥.
     */
    private fun decryptSafeKey() {
        val context = context ?: return
        val keyPair = FingerprintActivity.findKeyPair()
        if (keyPair == null) {
            context.showToast("未启用指纹安全项")
            return
        }
        FingerprintActivity.authenticate(BiometricPrompt(this,
            object : FingerprintActivity.AuthenticationCallback(context) {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    launch {
                        catchException({
                            val cipher = result.cryptoObject!!.cipher!!
                            // 读取安全密钥密文并解密
                            val ciphertext = readFile(safeKeyFile(context, FILE_KEY_FINGERPRINT))
                            val safeKey = RSA.decrypt(cipher, ciphertext)
                            // 初始化安全密钥
                            SafeKey.init(AES.generateKey(safeKey))
                            onVerifySuccess()
                        }, {
                            context.showToast("数据错误")
                            MyLog("指纹登入").w(it, true)
                        })
                    }
                }
            }
        ), "请验证指纹", keyPair.private)
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            decryptSafeKey()
        }
    }
}