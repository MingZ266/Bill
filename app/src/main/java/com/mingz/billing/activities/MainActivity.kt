package com.mingz.billing.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mingz.billing.R
import com.mingz.billing.fragments.BillingMainFragment
import com.mingz.billing.fragments.RecordExpenditureFragment
import com.mingz.billing.fragments.TestFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        /*supportFragmentManager.beginTransaction()
            .add(R.id.frame, BillingMainFragment.newInstance()).commit()*/
        /*supportFragmentManager.beginTransaction()
            .add(R.id.frame, TestFragment.newInstance()).commit()*/
        supportFragmentManager.beginTransaction()
            .add(R.id.frame, RecordExpenditureFragment.newInstance()).commit()
    }

    private fun initView() {}

    /*
    设计思路：
    数据按每三个月一分；
    数据以比特运算，按位取反（~）存储；
    数据通过设定的密码加密。
     */
}