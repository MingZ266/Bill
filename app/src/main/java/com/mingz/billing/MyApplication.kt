package com.mingz.billing

import android.app.Application
import com.mingz.billing.entities.Billing
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.Encryption
import java.io.File

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationContext.let {
            Encryption.init(it)
            DataSource.init(it)
            Billing.init(it)
            // 给予用户和系统提示
            Thread {
                val dir = it.getExternalFilesDir("") ?: return@Thread
                File(dir, "_本目录下文件非常重要，请勿删除").apply {
                    if (!exists()) createNewFile()
                }
                File(dir, "_Files in this directory are very important Please do not delete").apply {
                    if (!exists()) createNewFile()
                }
                File(dir, ".nomedia").apply {
                    if (!exists()) createNewFile()
                }
            }.run()
        }
    }
}