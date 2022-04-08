package com.mingz.billing.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.R
import com.mingz.billing.fragments.BillingMainFragment
import com.mingz.billing.fragments.TestFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        /*supportFragmentManager.beginTransaction()
            .add(R.id.frame, BillingMainFragment.newInstance()).commit()*/
        supportFragmentManager.beginTransaction()
            .add(R.id.frame, TestFragment.newInstance()).commit()
    }

    private fun initView() {}
}