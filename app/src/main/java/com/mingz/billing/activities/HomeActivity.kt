package com.mingz.billing.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.R
import com.mingz.billing.databinding.ActivityHomeBinding
import com.mingz.billing.fragments.*
import com.mingz.billing.ui.SwitchAnimView
import com.mingz.billing.utils.Constant
import com.mingz.billing.utils.DataSource

class HomeActivity : AppCompatActivity() {
    private val activity = this
    private val billing = HomeBillingFragment.newInstance()
    private val balance by lazy { HomeBalanceFragment.newInstance() }
    private val adminAccount by lazy { HomeAdminAccountFragment.newInstance() }
    private val adminSubject by lazy { HomeAdminSubjectFragment.newInstance() }

    private lateinit var binding: ActivityHomeBinding
    private var resumeHomeBilling = false

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
        // 数据初始化
        DataSource.initData(activity)
        initView()
        myListener()
    }

    override fun onResume() {
        super.onResume()
        if (resumeHomeBilling) {
            resumeHomeBilling = false
            replaceFragment(billing)
        }
    }

    private fun initView() {
        binding.title.text = billing.getTitle()
        supportFragmentManager.beginTransaction().add(R.id.frame, billing).commit()
    }

    private fun myListener() {
        binding.menu.setOnClickListener { binding.drawer.openDrawer(Gravity.START) }
        binding.addRecord.setOnClickListener {
            startActivity(Intent(activity, RecordActivity::class.java))
            resumeHomeBilling = true
        }
        // 菜单选项
        binding.menuLayout.browseBilling.setOnClickListener { replaceFragment(billing) }
        binding.menuLayout.browseBalance.setOnClickListener { replaceFragment(balance) }
        binding.menuLayout.settingAccount.setOnClickListener { replaceFragment(adminAccount) }
        binding.menuLayout.settingSubject.setOnClickListener { replaceFragment(adminSubject) }
    }

    private fun replaceFragment(fragment: HomeFragment) {
        binding.title.text = fragment.getTitle()
        supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
        binding.drawer.closeDrawer(Gravity.START)
    }
}
