package com.mingz.security.option

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import com.mingz.security.*
import com.mingz.security.CFG_FINGERPRINT_BOOL
import com.mingz.security.CFG_PASSWORD_BOOL
import com.mingz.security.FILE_KEY_PASSWORD
import com.mingz.security.R
import com.mingz.security.databinding.ActivityPasswordBinding
import com.mingz.share.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

class PasswordActivity : ByteSafetyOptionActivity(), CoroutineScope {
    override val coroutineContext = Dispatchers.Main
    override val filename = FILE_KEY_PASSWORD

    private val myLog by lazy(LazyThreadSafetyMode.NONE) { MyLog("密码安全项") }
    private val activity = this
    private val loading = Loading(activity) // 等待弹窗
    private lateinit var binding: ActivityPasswordBinding
    private lateinit var config: Config
    private var doneInit = false // 标记在点击启用/禁用后是否已完成初始化

    companion object {
        private const val MESSAGE_IS_EMPTY = "输入不能为空"
        private const val MESSAGE_NOT_EQUAL = "密码输入不一致"
        private const val MESSAGE_PASSWORD_ERROR = "密码错误"

        // 更新安全密钥密文
        @Suppress("unused") // 通过反射调用
        @JvmStatic
        fun whenSafeKeyUpdated(context: Context, safeKey: SecretKey) {
            whenSafeKeyUpdated(context, safeKey, FILE_KEY_PASSWORD)
        }

        /**
         * 验证密码，进而解密安全密钥.
         * @return 若密码正确，则返回解密后的安全密钥，否则返回null
         */
        @JvmStatic
        suspend fun decryptSafeKey(context: Context, password: String): ByteArray? =
            decryptSafeKey(context, FILE_KEY_PASSWORD, toByteArray(password))

        // 将密码转为字节流
        private fun toByteArray(password: String) = password.toByteArray(StandardCharsets.UTF_8)
    }

    // 进入该页面后输入区域的变化， 若：
    // - 未启用：隐藏输入区域 --点击启用--> 显示：设置密码 --设定成功--> 显示：修改密码
    // - 已启用：显示：修改密码 --点击禁用--> 显示：输入密码 --验证成功--> 隐藏输入区域
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化配置文件
        config = Config(activity, FILE_CONFIG_SAFETY)
        // 初始化视图
        if (savedInstanceState != null) {
            clearInput()
            clearFocus()
        }
        val enable = config[CFG_PASSWORD_BOOL, false]
        binding.enable.enable.initChecked(enable)
        if (enable) {
            alterPassword()
        } else { // 隐藏密码操作区域
            binding.passwdLayout.visibility = View.GONE
        }
        // 初始化监听
        with(binding.enable.enable) {
            setOnClickListener { if (isChecked) disable() else enable() }
        }
    }

    override fun writeConfig(value: Boolean) {
        config[CFG_PASSWORD_BOOL] = value
    }

    // 设置密码以启用密码安全项
    private fun enable() {
        activity.showToast("请设置您的密码")
        if (doneInit) return
        with(binding) {
            // 设置动画
            passwdLayout.animation = AlphaAnimation(0.0f, 1.0f).apply { duration = 300 }
            // 显示：设置密码
            passwdLayout.visibility = View.VISIBLE
            titleOld.visibility = View.GONE
            oldPasswd.visibility = View.GONE
            confirmPasswd.visibility = View.VISIBLE
            titleNew.text = getString(R.string.passwdTitle)
            newPasswd.hint = getString(R.string.passwdHint)
            confirm.text = getString(com.mingz.share.R.string.ok)
            // 设置监听
            confirm.setOnClickListener {
                clearFocus()
                val password = newPasswd.text.toString()
                // 检查输入
                confirmPasswd.text.toString().let {
                    if (password.isEmpty() || it.isEmpty()) {
                        activity.showToast(MESSAGE_IS_EMPTY)
                        return@setOnClickListener
                    }
                    if (password != it) {
                        activity.showToast(MESSAGE_NOT_EQUAL)
                        return@setOnClickListener
                    }
                }
                // 设置密码
                launch {
                    loading.show("正在启用")
                    catchException({
                        requestEnable(activity, toByteArray(password))
                        // 反馈已启用
                        binding.enable.enable.isChecked = true
                        doneInit = false // 重置标记
                        // 变更为修改密码区域
                        clearInput()
                        alterPassword()
                    }, {
                        activity.showToast("启用失败")
                        myLog.w("密码启用失败", it, true)
                    })
                    loading.dismiss()
                }
            }
        }
        doneInit = true
    }

    // 验证密码以禁用密码安全项
    private fun disable() {
        val notEnablePattern = !config[CFG_PATTERN_BOOL, false]
        // 当指纹安全项已启用时，若未启用图案安全项，将不允许禁用
        if (config[CFG_FINGERPRINT_BOOL, false] && notEnablePattern) {
            activity.showToast("请在禁用指纹后再次尝试")
            return
        }
        activity.showToast("请输入密码")
        if (doneInit) return
        with(binding) {
            // 设置动画：“修改密码”逐渐透明，然后“输入密码“逐渐不透明
            val animator = ValueAnimator.ofFloat(0.0f, 1.0f, 2.0f)
            animator.duration = 300
            animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                private var noChange = true

                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val value = animation.animatedValue as Float
                    val alpha: Float
                    if (value >= 1.0f) { // [1.0, 2.0]，逐渐不透明
                        alpha = value - 1.0f
                        if (noChange) {
                            noChange = false
                            // 显示：输入密码
                            passwdLayout.visibility = View.VISIBLE
                            titleOld.visibility = View.GONE
                            oldPasswd.visibility = View.GONE
                            confirmPasswd.visibility = View.GONE
                            titleNew.text = getString(R.string.passwdTitle)
                            newPasswd.hint = getString(R.string.passwdHint)
                            confirm.text = getString(com.mingz.share.R.string.ok)
                        }
                    } else { // [0.0, 1.0)，逐渐透明
                        alpha = 1.0f - value
                    }
                    passwdLayout.alpha = alpha
                }
            })
            animator.start()
            // 设置监听
            confirm.setOnClickListener {
                clearFocus()
                // 检查输入
                val password = newPasswd.text.toString()
                if (password.isEmpty()) {
                    activity.showToast(MESSAGE_IS_EMPTY)
                    return@setOnClickListener
                }
                launch {
                    loading.show("正在禁用")
                    catchException({
                        // 若未启用图案安全项，则代表将没有安全项被启用
                        if (requestDisable(activity, toByteArray(password), notEnablePattern)) {
                            // 反馈已禁用
                            binding.enable.enable.isChecked = false
                            doneInit = false // 重置标记
                            // 隐藏输入区域
                            clearInput()
                            binding.passwdLayout.visibility = View.GONE
                        } else { // 密码错误
                            activity.showToast(MESSAGE_PASSWORD_ERROR)
                        }
                    }, {
                        activity.showToast("操作失败")
                        myLog.w("密码禁用失败", it, true)
                    })
                    loading.dismiss()
                }
            }
        }
        doneInit = true
    }

    // 密码安全项启用后用于修改密码
    private fun alterPassword() {
        with(binding) {
            // 显示：修改密码
            passwdLayout.visibility = View.VISIBLE
            titleOld.visibility = View.VISIBLE
            oldPasswd.visibility = View.VISIBLE
            confirmPasswd.visibility = View.VISIBLE
            titleNew.text = getString(R.string.newPasswdTitle)
            newPasswd.hint = getString(R.string.newPasswdHint)
            confirm.text = getString(R.string.alterButton)
            // 设置监听
            confirm.setOnClickListener {
                clearFocus()
                // 检查输入
                val oldPassword = oldPasswd.text.toString()
                val newPassword = newPasswd.text.toString()
                confirmPasswd.text.toString().let {
                    if (oldPassword.isEmpty() || newPassword.isEmpty() || it.isEmpty()) {
                        activity.showToast(MESSAGE_IS_EMPTY)
                        return@setOnClickListener
                    }
                    if (newPassword != it) {
                        activity.showToast(MESSAGE_NOT_EQUAL)
                        return@setOnClickListener
                    }
                    if (newPassword == oldPassword) {
                        activity.showToast("新密码与旧密码相同")
                        return@setOnClickListener
                    }
                }
                // 修改密码
                launch {
                    catchException({
                        if (requestAlter(activity, toByteArray(oldPassword), toByteArray(newPassword))) {
                            // 反馈已修改
                            clearInput()
                            activity.showToast("密码已修改")
                        } else { // 旧密码错误
                            activity.showToast(MESSAGE_PASSWORD_ERROR)
                        }
                    }, {
                        activity.showToast("密码修改失败")
                        myLog.v("密码修改失败", it, true)
                    })
                }
            }
        }
    }

    // 清除可能存在的输入
    private fun clearInput() {
        with(binding) {
            oldPasswd.setText("")
            newPasswd.setText("")
            confirmPasswd.setText("")
        }
    }

    // 清除焦点并收起键盘
    private fun clearFocus() {
        with(binding) {
            oldPasswd.clearFocus()
            newPasswd.clearFocus()
            confirmPasswd.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(oldPasswd.windowToken, 0)
            imm.hideSoftInputFromWindow(newPasswd.windowToken, 0)
            imm.hideSoftInputFromWindow(confirmPasswd.windowToken, 0)
        }
    }
}