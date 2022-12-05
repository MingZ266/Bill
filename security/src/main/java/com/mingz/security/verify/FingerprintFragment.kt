package com.mingz.security.verify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import com.mingz.security.*
import com.mingz.security.databinding.FragmentFingerprintBinding
import com.mingz.security.option.FingerprintActivity
import com.mingz.share.MyLog
import com.mingz.share.catchException
import com.mingz.share.readFile
import com.mingz.share.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class FingerprintFragment : SafetyVerifyFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    override val mTag = TAG

    private lateinit var binding: FragmentFingerprintBinding

    companion object {
        const val TAG = CFG_FINGERPRINT_BOOL
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