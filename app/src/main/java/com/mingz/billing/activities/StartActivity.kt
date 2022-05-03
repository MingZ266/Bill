package com.mingz.billing.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.R
import com.mingz.billing.databinding.ActivityStartBinding
import com.mingz.billing.ui.SwitchAnimView
import com.mingz.billing.utils.Config
import com.mingz.billing.utils.Constant
import java.util.*

class StartActivity : AppCompatActivity() {
    private val activity = this
    private lateinit var binding: ActivityStartBinding
    private var finishOnStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val proverb = resources.getStringArray(R.array.proverb)
        binding.proverb.text = proverb[Random().nextInt(proverb.size)]

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (Config(activity)[Config.CFG_ENCRYPT_BOOLEAN, false]) {
                        // 设置了密码，需要先解锁
                        binding.start.visibility = View.GONE
                        binding.verify.visibility = View.VISIBLE
                        // TODO: 解锁界面
                    } else {
                        // 未设置密码，直接进入
                        binding.halfSwitchAnim.startAnimBeforeSwitch(object : SwitchAnimView.AnimListener() {
                            override fun onStop() {
                                finishOnStop = true
                                startActivity(Intent(activity, HomeActivity::class.java).apply {
                                    putExtra(Constant.KEY_ANIM_TYPE_INT, binding.halfSwitchAnim.getType())
                                    putExtra(Constant.KEY_ANIM_DIRECTION_INT, binding.halfSwitchAnim.getDirection())
                                    flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                                })
                            }
                        })
                    }
                }
            }
        }, 800)
    }

    override fun onStop() {
        super.onStop()
        if (finishOnStop) {
            activity.finish()
        }
    }
}