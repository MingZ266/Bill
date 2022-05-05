package com.mingz.billing

import android.app.Application
import com.mingz.billing.entities.Billing
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.Encryption

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationContext.let {
            Encryption.init(it)
            DataSource.init(it)
            Billing.init(it)
        }
    }
}