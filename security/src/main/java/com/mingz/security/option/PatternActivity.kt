package com.mingz.security.option

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mingz.security.CFG_FINGERPRINT_BOOL
import com.mingz.security.CFG_PASSWORD_BOOL
import com.mingz.security.CFG_PATTERN_BOOL
import com.mingz.security.FILE_KEY_PATTERN
import com.mingz.security.databinding.ActivityPatternBinding
import com.mingz.share.*
import com.mingz.share.ui.PatternDrawing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Arrays
import javax.crypto.SecretKey

class PatternActivity : ByteSafetyOptionActivity(), CoroutineScope {
    override val coroutineContext = Dispatchers.Main
    override val filename = FILE_KEY_PATTERN

    private val myLog = MyLog("图案安全项")
    private val activity = this
    private val loading = Loading(activity) // 等待弹窗
    private lateinit var binding: ActivityPatternBinding
    private lateinit var config: Config
    private var doneInit = false // 标记在点击启用/禁用后是否已完成初始化
    private var doAlter = false // 标记当前是否正在修改图案

    companion object {
        internal const val DELAY_REDRAW = 500L // 重新绘制图案的延时时间（ms）
        // 文本框消息
        private const val MESSAGE_NEED_DRAW = "请绘制您的图案"
        private const val MESSAGE_AGAIN_DRAW = "请再次绘制您的图案"
        private const val MESSAGE_UNLOCK_ON_ALTER = "请绘制图案以修改"
        // TOAST消息
        internal const val MESSAGE_PATTERN_ERROR = "图案错误"

        // 更新安全密钥密文
        @Suppress("unused") // 通过反射调用
        @JvmStatic
        fun whenSafeKeyUpdated(context: Context, safeKey: SecretKey) {
            whenSafeKeyUpdated(context, safeKey, FILE_KEY_PATTERN)
        }

        /**
         * 验证图案，进而解密安全密钥.
         * @return 若图案正确，则返回解密后的安全密钥，否则返回null
         */
        @JvmStatic
        suspend fun decryptSafeKey(context: Context, pattern: IntArray) =
            decryptSafeKey(context, FILE_KEY_PATTERN, toByteArray(pattern))

        // 将图案转为字节流
        private fun toByteArray(pattern: IntArray) = ByteArrayOutputStream(pattern.size).use {
            for (number in pattern) {
                it.write(number)
            }
            it.toByteArray()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatternBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化配置文件
        config = Config(activity, FILE_CONFIG_SAFETY)
        // 初始化视图
        val enable = config[CFG_PATTERN_BOOL, false]
        binding.enable.enable.initChecked(enable)
        if (enable) { // 显示“修改图案”选项
            binding.alterPattern.visibility = View.VISIBLE
        }
        // 设置监听
        with(binding.enable.enable) {
            setOnClickListener { if (isChecked) disable() else enable() }
        }
        binding.alterPattern.setOnClickListener { alter() }
    }

    override fun writeConfig(value: Boolean) {
        config[CFG_PATTERN_BOOL] = value
    }

    // 显示图案绘制区域
    private fun showPatterArea() {
        val animator = ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.duration = 300
        animator.addUpdateListener {
            val alpha = it.animatedValue as Float
            binding.message.alpha = alpha
            binding.pattern.alpha = alpha
        }
        animator.start()
        // 显示图案绘制区域
        binding.message.visibility = View.VISIBLE
        binding.pattern.visibility = View.VISIBLE
    }

    // 设置图案以启用安全项
    private fun enable() {
        if (doneInit) return
        showPatterArea()
        // 设置回调
        binding.message.text = MESSAGE_NEED_DRAW
        binding.pattern.setCallback(object : RepeatDrawCallback(binding.message, binding.pattern) {
            override fun usePattern(pattern: IntArray) { // 启用安全项
                launch {
                    loading.show("正在启用")
                    catchException({
                        requestEnable(activity, toByteArray(pattern))
                        // 反馈已启用
                        binding.enable.enable.isChecked = true
                        // 隐藏图案绘制区域
                        binding.message.visibility = View.GONE
                        binding.pattern.visibility = View.GONE
                        // 显示修改选项
                        binding.alterPattern.visibility = View.VISIBLE
                        // 重置标记
                        doneInit = false
                    }, {
                        activity.showToast("启用失败")
                        myLog.w("图案启用失败", it, true)
                    })
                    loading.dismiss()
                }
            }
        })
        doneInit = true
    }

    // 绘制图案以禁用安全项
    private fun disable() {
        if (doAlter) {
            activity.showToast("请在取消修改后再次尝试")
            return
        }
        val notEnablePassword = !config[CFG_PASSWORD_BOOL, false]
        // 当指纹安全项已启用时，若未启用密码安全项，将不允许禁用
        if (config[CFG_FINGERPRINT_BOOL, false] && notEnablePassword) {
            activity.showToast("请在禁用指纹后再次尝试")
            return
        }
        if (doneInit) return
        // 设置动画以先隐藏修改选项再显示绘制区域
        val animator = ValueAnimator.ofFloat(0.0f, 1.0f, 2.0f)
        animator.duration = 300
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            private var noChange = true

            override fun onAnimationUpdate(animation: ValueAnimator) {
                val value = animation.animatedValue as Float
                if (value >= 1.0f) { // [1.0, 2.0]，逐渐不透明（绘制区域）
                    val alpha = value - 1.0f
                    binding.message.alpha = alpha
                    binding.pattern.alpha = alpha
                    if (noChange) {
                        noChange = false
                        // 隐藏修改选项，显示绘制区域
                        binding.alterPattern.visibility = View.GONE
                        binding.message.visibility = View.VISIBLE
                        binding.pattern.visibility = View.VISIBLE
                    }
                } else { // [0.0, 1.0)，逐渐透明（修改选项）
                    binding.alterPattern.alpha = 1.0f - value
                }
            }
        })
        animator.start()
        // 设置回调
        binding.message.text = "请绘制图案以禁用"
        binding.pattern.setCallback(object : PatternDrawing.Callback {
            override fun onResult(pattern: IntArray) {
                launch {
                    loading.show("正在禁用")
                    catchException({
                        // 若未启用密码安全项，则代表将没有安全项被启用
                        if (requestDisable(activity, toByteArray(pattern), notEnablePassword)) {
                            // 隐藏绘制区域
                            binding.pattern.visibility = View.GONE
                            binding.message.visibility = View.GONE
                            // 反馈已禁用
                            binding.enable.enable.isChecked = false
                            // 重置标记
                            doneInit = false
                        } else { // 图案错误
                            onLess()
                        }
                    }, {
                        activity.showToast("操作失败")
                        myLog.w("图案禁用失败", it, true)
                    })
                    loading.dismiss()
                }
            }

            // 视作图案错误
            override fun onLess() {
                activity.showToast(MESSAGE_PATTERN_ERROR)
                // 短暂延时后重新绘制
                binding.pattern.postDelayed({
                    binding.pattern.reDraw()
                }, DELAY_REDRAW)
            }
        })
        doneInit = true
    }

    // 修改图案
    private fun alter() {
        if (doAlter) { // 当前正在修改，取消修改
            cancelAlter()
            return
        }
        // 显示绘制区域，切换修改选项样式
        showPatterArea()
        binding.alterPattern.background = ContextCompat.getDrawable(activity, R.drawable.bg_stroke_and_fill)
        binding.alterPattern.setDrawables(null, null,
            ContextCompat.getDrawable(activity, R.drawable.ic_close), null)
        // 设置回调
        binding.message.text = MESSAGE_UNLOCK_ON_ALTER
        binding.pattern.setCallback(object : RepeatDrawCallback(binding.message, binding.pattern) {
            // 旧的解锁图案
            private var oldPattern: IntArray? = null

            override fun onResult(pattern: IntArray) {
                if (oldPattern == null) { // 验证图案是否正确
                    launch {
                        if (decryptSafeKey(activity, pattern) == null) { // 图案错误
                            onPatternError()
                        } else { // 图案正确
                            oldPattern = pattern // 记录旧图案
                            binding.message.text = MESSAGE_NEED_DRAW
                            binding.pattern.reDraw() // 绘制新图案
                        }
                    }
                } else { // 绘制的新图案
                    checkResult(pattern)
                }
            }

            override fun onLess() {
                if (oldPattern == null) { // 绘制的旧图案，视作图案错误
                    onPatternError()
                } else { // 绘制的新图案
                    tooLess()
                }
            }

            // 旧图案错误
            private fun onPatternError() {
                activity.showToast(MESSAGE_PATTERN_ERROR)
                // 短暂延时后重新绘制
                binding.pattern.postDelayed({
                    binding.pattern.reDraw()
                }, DELAY_REDRAW)
            }

            // 修改图案
            override fun usePattern(pattern: IntArray) {
                launch {
                    catchException({
                        if (requestAlter(activity, toByteArray(oldPattern!!), toByteArray(pattern))) {
                            // 反馈已修改
                            activity.showToast("图案已修改")
                            // 隐藏图案绘制区域及重置修改选项样式
                            binding.message.visibility = View.GONE
                            binding.pattern.visibility = View.GONE
                            resetAlterButtonStyle()
                            // 重置标记
                            doAlter = false
                        } else {
                            activity.showToast(MESSAGE_PATTERN_ERROR)
                            // 从验证旧图案处重新开始
                            oldPattern = null
                            firstPattern = null
                            binding.message.text = MESSAGE_UNLOCK_ON_ALTER
                        }
                    }, {
                        activity.showToast("图案修改失败")
                        myLog.v("图案修改失败", it, true)
                    })
                }
            }
        })
        doAlter = true // 标记当前正在修改
    }

    private fun cancelAlter() {
        // 设置动画以隐藏绘制区域并重置修改选项样式
        val animator = ValueAnimator.ofFloat(1.0f, 0.0f)
        animator.duration = 300
        animator.addUpdateListener {
            val alpha = it.animatedValue as Float
            binding.message.alpha = alpha
            binding.pattern.alpha = alpha
            if (alpha <= 0.0f) { // 动画结束
                // 隐藏绘制区域
                binding.message.visibility = View.GONE
                binding.pattern.visibility = View.GONE
                // 重置绘制状态
                binding.pattern.reDraw()
                // 重置修改选项样式
                resetAlterButtonStyle()
                // 重置标记
                doAlter = false
            }
        }
        animator.start()
    }

    // 重置修改选项样式
    private fun resetAlterButtonStyle() {
        // 重置背景和图标
        binding.alterPattern.background = ContextCompat.getDrawable(activity, R.drawable.bg_fill)
        binding.alterPattern.setDrawables(null, null,
            ContextCompat.getDrawable(activity, R.drawable.ic_edit), null)
    }

    // 当需要重复绘制两次图案时的回调实现
    private abstract class RepeatDrawCallback(
        private val message: TextView, private val thePattern: PatternDrawing
    ) : PatternDrawing.Callback {
        // 第一次绘制的图案
        protected var firstPattern: IntArray? = null

        // 应在一次图案绘制返回结果时调用
        protected fun checkResult(pattern: IntArray) {
            if (firstPattern == null) { // 第一次绘制，记录图案
                firstPattern = pattern
                // 短暂延时后再次绘制
                thePattern.postDelayed({
                    message.text = MESSAGE_AGAIN_DRAW
                    thePattern.reDraw()
                }, DELAY_REDRAW)
            } else { // 第二次绘制，检查是否一致
                if (Arrays.equals(firstPattern, pattern)) {
                    // 图案一致，使用绘制的图案
                    usePattern(pattern)
                    // 短暂延时后重置绘制状态
                    thePattern.postDelayed({
                        thePattern.reDraw()
                    }, DELAY_REDRAW)
                } else { // 图案不一致
                    message.text = "图案不一致"
                    // 短暂延时后重新绘制
                    thePattern.postDelayed({
                        firstPattern = null
                        message.text = MESSAGE_NEED_DRAW
                        thePattern.reDraw()
                    }, DELAY_REDRAW)
                }
            }
        }

        // 应在一次图案绘制返回连接点过少时调用
        protected fun tooLess() {
            message.text = "请至少连接4个点"
            // 短暂延时后重新绘制本次图案
            thePattern.postDelayed({
                message.text = if (firstPattern == null) MESSAGE_NEED_DRAW else MESSAGE_AGAIN_DRAW
                thePattern.reDraw()
            }, DELAY_REDRAW)
        }

        // 在重复绘制的图案一致时将调用
        protected abstract fun usePattern(pattern: IntArray)

        override fun onResult(pattern: IntArray) { checkResult(pattern) }

        override fun onLess() { tooLess() }
    }
}