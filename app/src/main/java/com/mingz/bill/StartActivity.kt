package com.mingz.bill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mingz.bill.databinding.ActivityStartBinding
import com.mingz.security.SafetyVerifyActivity
import com.mingz.security.checkSafetyOption
import com.mingz.security.verify.SafetyVerifyFragment
import com.mingz.share.KEY_ANIM_DIRECTION_INT
import com.mingz.share.KEY_ANIM_TYPE_INT
import com.mingz.share.ui.SwitchAnimView
import kotlinx.coroutines.*

class StartActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext = Dispatchers.Main

    private val activity = this
    private var finishOnStop = false
    private lateinit var binding: ActivityStartBinding
    // 并行任务
    private lateinit var check: Deferred<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 查询是否需要验证安全项以解锁安全密钥
        check = async { checkSafetyOption(activity) }
        launch {
            delay(500) // 短暂延时，用以展示起始页
            // 播放前半部分切换动画
            binding.switchAnim.startAnimBeforeSwitch(object : SwitchAnimView.AnimListener() {
                override fun onStop() {
                    skip() // 动画结束后跳转
                }
            })
        }
    }

    // 跳转
    private fun skip() {
        launch {
            // 等待查询任务完成
            val goal = if (check.await()) { // 需要验证安全项
                initVerifyCallback()
                SafetyVerifyActivity::class.java
            } else { // 不需要验证安全项
                TempActivity::class.java // TODO: 跳转到主页
            }
            startActivity(Intent(activity, goal).apply {
                // 传递动画信息
                with(binding.switchAnim) {
                    putExtra(KEY_ANIM_TYPE_INT, getType())
                    putExtra(KEY_ANIM_DIRECTION_INT, getDirection())
                }
                flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            })
            finishOnStop = true
        }
    }

    // 初始化安全项验证结果回调
    private fun initVerifyCallback() {
        SafetyVerifyFragment.setVerifyCallback(object : SafetyVerifyFragment.VerifyCallback {
            override fun onSuccess(context: Context) {
                // TODO: 跳转到主页
                context.startActivity(Intent(context, TempActivity::class.java).apply {
                    // 清除页面
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        })
    }

    override fun onStop() {
        super.onStop()
        if (finishOnStop) {
            activity.finish()
        }
    }
}