package com.mingz.security

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mingz.security.databinding.ActivitySafetyVerifyBinding
import com.mingz.security.verify.FingerprintFragment
import com.mingz.security.verify.PasswordFragment
import com.mingz.security.verify.PatternFragment
import com.mingz.security.verify.SafetyVerifyFragment
import com.mingz.share.Config
import com.mingz.share.FILE_CONFIG_SAFETY
import com.mingz.share.KEY_ANIM_DIRECTION_INT
import com.mingz.share.KEY_ANIM_TYPE_INT
import com.mingz.share.ui.CheckedImageView
import com.mingz.share.ui.SwitchAnimView

class SafetyVerifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySafetyVerifyBinding
    private var currentFragment: SafetyVerifyFragment? = null // 当前选中的安全项
    private var currentCheckedIcon: CheckedImageView? = null // 当前选中项对应的图标

    companion object {
        private const val KEY_TAG = "verify_tag" // 存储tag的键
        private const val KEY_PASSWORD_ICON = PasswordFragment.TAG // 存储密码图标是否显示的键
        private const val KEY_PATTERN_ICON = PatternFragment.TAG // 存储图案图标是否显示的键
        private const val KEY_FINGERPRINT_ICON = FingerprintFragment.TAG // 存储指纹图标是否显示的键
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySafetyVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            playSwitchAnim()
            initSafetyOption()
        } else { // 因屏幕旋转等原因恢复数据
            val currentTag = savedInstanceState.getString(KEY_TAG)
            currentFragment = supportFragmentManager.findFragmentByTag(currentTag) as SafetyVerifyFragment?
            // 恢复视图数据
            binding.switchAnim.visibility = View.GONE // 去除动画层遮盖
            if (savedInstanceState.getBoolean(KEY_PASSWORD_ICON, false)) {
                binding.password.visibility = View.VISIBLE
            }
            if (savedInstanceState.getBoolean(KEY_PATTERN_ICON, false)) {
                binding.pattern.visibility = View.VISIBLE
            }
            if (savedInstanceState.getBoolean(KEY_FINGERPRINT_ICON, false)) {
                binding.fingerprint.visibility = View.VISIBLE
            }
            when(currentTag) {
                PasswordFragment.TAG -> currentCheckedIcon = binding.password
                PatternFragment.TAG -> currentCheckedIcon = binding.pattern
                FingerprintFragment.TAG -> currentCheckedIcon = binding.fingerprint
            }
            currentCheckedIcon?.isChecked = true // 设为选中
        }
        // 点击图标切换安全项
        with(binding.password) { setOnClickListener { toggleOption(PasswordFragment.TAG, this) } }
        with(binding.pattern) { setOnClickListener { toggleOption(PatternFragment.TAG, this) } }
        with(binding.fingerprint) { setOnClickListener { toggleOption(FingerprintFragment.TAG, this) } }
    }

    // 衔接播放后半部分的切换动画
    private fun playSwitchAnim() {
        with(intent) {
            binding.switchAnim.startAnimAfterSwitch(
                getIntExtra(KEY_ANIM_TYPE_INT, SwitchAnimView.TYPE_RANDOM),
                getIntExtra(KEY_ANIM_DIRECTION_INT, SwitchAnimView.DIRECTION_RANDOM),
                object : SwitchAnimView.AnimListener() {
                    override fun onStop() {
                        binding.switchAnim.visibility = View.GONE
                    }
                }
            )
        }
    }

    // 初始化可选验证的安全项图标，应当至少有一项
    private fun initSafetyOption() {
        val config = Config(this, FILE_CONFIG_SAFETY)
        val hasPassword = config[CFG_PASSWORD_BOOL, false] // 有无密码安全项
        val hasPattern = config[CFG_PATTERN_BOOL, false] // 有无图案安全项
        val hasFingerprint = config[CFG_FINGERPRINT_BOOL, false] // 有无指纹安全项
        // 首选安全项：指纹 > 图案 > 密码
        if (hasPassword) {
            currentFragment = PasswordFragment()
            binding.password.let {
                it.visibility = View.VISIBLE
                currentCheckedIcon = it
            }
        }
        if (hasPattern) {
            currentFragment = PatternFragment()
            binding.pattern.let {
                it.visibility = View.VISIBLE
                currentCheckedIcon = it
            }
        }
        if (hasFingerprint) {
            currentFragment = FingerprintFragment()
            binding.fingerprint.let {
                it.visibility = View.VISIBLE
                currentCheckedIcon = it
            }
        }
        // 初始化选中当前安全项
        if (currentFragment != null) {
            supportFragmentManager.beginTransaction().add(R.id.frame,
                currentFragment!!, currentFragment!!.mTag).commit()
            currentCheckedIcon!!.isChecked = true // 设置选中
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存当前选中项的tag
        outState.putString(KEY_TAG, currentFragment?.mTag)
        // 保存各图标是否显示
        outState.putBoolean(KEY_PASSWORD_ICON, binding.password.visibility == View.VISIBLE)
        outState.putBoolean(KEY_PATTERN_ICON, binding.pattern.visibility == View.VISIBLE)
        outState.putBoolean(KEY_FINGERPRINT_ICON, binding.fingerprint.visibility == View.VISIBLE)
    }

    // 切换选中的安全项
    private fun toggleOption(goalTag: String, goalIcon: CheckedImageView) {
        val lastFragment = currentFragment ?: return
        if (goalTag == lastFragment.mTag) return
        with(supportFragmentManager) {
            val transaction = beginTransaction()
            // 隐藏当前安全项
            transaction.hide(lastFragment)
            lastFragment.onHide()
            // 查找是否已添加过目标
            var goalFragment = findFragmentByTag(goalTag) as SafetyVerifyFragment?
            if (goalFragment == null) { // 尚未添加，将添加目标安全项
                goalFragment = when(goalTag) {
                    PasswordFragment.TAG -> PasswordFragment()
                    PatternFragment.TAG -> PatternFragment()
                    FingerprintFragment.TAG -> FingerprintFragment()
                    else -> return
                }
                transaction.add(R.id.frame, goalFragment, goalTag)
            } else { // 已添加，显示目标安全项
                goalFragment.onShow()
                transaction.show(goalFragment)
            }
            transaction.commit()
            // 记录当前选中的安全项
            currentFragment = goalFragment
        }
        // 切换选中的安全项图标
        currentCheckedIcon?.isChecked = false
        goalIcon.isChecked = true
        currentCheckedIcon = goalIcon
    }
}