package com.mingz.billing

import android.app.Application
import com.mingz.billing.utils.DataSource

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataSource.INSTANCE.init(applicationContext)
    }
}