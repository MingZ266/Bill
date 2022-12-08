package com.mingz.security

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import com.mingz.security.databinding.ActivitySafetyOptionBinding
import com.mingz.security.databinding.DialogTipEnrolledBiometricBinding
import com.mingz.security.option.FingerprintActivity
import com.mingz.security.option.PasswordActivity
import com.mingz.security.option.PatternActivity
import com.mingz.share.DialogPack
import com.mingz.share.MyLog

class SafetyOptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySafetyOptionBinding
    private val activity = this
    private val tipDialog = DialogPack(activity, object : DialogPack.Creator<DialogTipEnrolledBiometricBinding> {
        override fun createBinding() = DialogTipEnrolledBiometricBinding.inflate(LayoutInflater.from(activity))

        override fun initDialog(dialog: AlertDialog, binding: DialogTipEnrolledBiometricBinding) {
            dialog.setCanceledOnTouchOutside(false)
            dialog.setOnDismissListener { showTip = false }
            binding.cancel.setOnClickListener { dialog.dismiss() }
            binding.confirm.setOnClickListener {
                dialog.dismiss()
                // 跳转到系统设置
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    })

    companion object {
        private var showTip = true // 是否显示提示弹窗
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySafetyOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.password.setOnClickListener { startActivity(Intent(this, PasswordActivity::class.java)) }

        binding.pattern.setOnClickListener { startActivity(Intent(this, PatternActivity::class.java)) }

        binding.fingerprint.setOnClickListener { startActivity(Intent(this, FingerprintActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        val status = BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (status == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.fingerprint.visibility = View.VISIBLE
        } else {
            binding.fingerprint.visibility = View.GONE
            MyLog("SafeOption").v("不支持生物识别（API：${Build.VERSION.SDK_INT}，错误码：$status）")
            // 设备支持生物识别但用户没有注册生物特征
            if (showTip && status == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                tipDialog.show()
            }
        }
    }
}