package com.mingz.security.verify

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.mingz.security.AES
import com.mingz.security.CFG_PASSWORD_BOOL
import com.mingz.security.SafeKey
import com.mingz.security.databinding.FragmentPasswordBinding
import com.mingz.security.option.PasswordActivity
import com.mingz.share.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PasswordFragment : SafetyVerifyFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    override val mTag = TAG

    private lateinit var binding: FragmentPasswordBinding

    companion object {
        const val TAG = CFG_PASSWORD_BOOL
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        binding.confirm.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val password: String
            with(binding.password) {
                password = text.toString()
                // 清除焦点，收起键盘
                clearFocus()
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(windowToken, 0)
            }
            // 检查输入
            if (password.isEmpty()) {
                context.showToast("输入不能为空")
                return@setOnClickListener
            }
            // 验证密码
            launch {
                val safeKey = PasswordActivity.decryptSafeKey(context, password)
                if (safeKey == null) {
                    context.showToast("密码错误")
                } else { // 密码正确
                    SafeKey.init(AES.generateKey(safeKey))
                    onVerifySuccess()
                }
            }
        }
    }
}