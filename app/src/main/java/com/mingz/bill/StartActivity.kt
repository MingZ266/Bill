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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 播放前半部分切换动画
        binding.root.startAnimBeforeSwitch(object : SwitchAnimView.AnimListener() {
            private lateinit var check: Deferred<Boolean>

            override fun onStart() {
                // 查询是否需要验证安全项以解锁安全密钥
                check = async { checkSafetyOption(activity) }
            }

            override fun onStop() {
                launch {
                    // 确保查询任务完成
                    val goal = if (check.await()) { // 需要验证安全项
                        SafetyVerifyFragment.setVerifyCallback(object : SafetyVerifyFragment.VerifyCallback {
                            override fun onSuccess(context: Context) {
                                // TODO: 跳转到主页
                                startActivity(Intent(context, TempActivity::class.java).apply {
                                    // 清除页面
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                        })
                        SafetyVerifyActivity::class.java
                    } else { // 不需要验证安全项
                        TempActivity::class.java // TODO: 跳转到主页
                    }
                    startActivity(Intent(activity, goal).apply {
                        with(binding.root) {
                            putExtra(KEY_ANIM_TYPE_INT, getType())
                            putExtra(KEY_ANIM_DIRECTION_INT, getDirection())
                        }
                        flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    })
                    finishOnStop = true
                }
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