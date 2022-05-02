package com.mingz.billing.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.R
import com.mingz.billing.databinding.ActivityMainBinding
import com.mingz.billing.fragments.MainBillingFragment
import com.mingz.billing.fragments.MainFragment
import com.mingz.billing.utils.Tools

class MainActivity : AppCompatActivity() {
    private val activity = this
    private val billing by lazy { MainBillingFragment.newInstance() }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Thread {
            Tools.showToastOnUiThread(activity, "吐司弹窗")
        }.start()
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

    private fun replaceFragment(fragment: MainFragment) {
        binding.title.text = fragment.getTitle()
        supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
    }
}
