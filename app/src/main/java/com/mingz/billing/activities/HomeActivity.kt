package com.mingz.billing.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.R
import com.mingz.billing.databinding.ActivityHomeBinding
import com.mingz.billing.fragments.HomeBillingFragment
import com.mingz.billing.fragments.HomeFragment
import com.mingz.billing.ui.SwitchAnimView
import com.mingz.billing.utils.Constant

class HomeActivity : AppCompatActivity() {
    private val activity = this
    private val billing by lazy { HomeBillingFragment.newInstance() }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 衔接切换动画
        with (intent) {
            binding.halfSwitchAnim.startAnimAfterSwitch(
                getIntExtra(Constant.KEY_ANIM_TYPE_INT, SwitchAnimView.TYPE_RANDOM),
                getIntExtra(Constant.KEY_ANIM_DIRECTION_INT, SwitchAnimView.DIRECTION_RANDOM),
                object : SwitchAnimView.AnimListener() {
                    override fun onStop() {
                        binding.halfSwitchAnim.visibility = View.GONE
                    }
                }
            )
        }
        initView()
        myListener()
    }

    private fun initView() {
        binding.title.text = billing.getTitle()
        supportFragmentManager.beginTransaction().add(R.id.frame, billing).commit()
    }

    private fun myListener() {
        binding.menu.setOnClickListener {
            binding.drawer.openDrawer(Gravity.START)
        }
        binding.addRecord.setOnClickListener {
            startActivity(Intent(activity, RecordActivity::class.java))
        }
    }

    private fun replaceFragment(fragment: HomeFragment) {
        binding.title.text = fragment.getTitle()
        supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
    }
}
