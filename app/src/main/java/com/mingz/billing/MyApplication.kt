package com.mingz.billing

import android.app.Application
import com.mingz.billing.entities.Billing
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.Encryption
import com.mingz.billing.utils.MyLog
import com.mingz.billing.utils.Tools

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context = applicationContext
        Encryption.init(context)
        DataSource.init(context)
        Billing.init(context)
    }
}