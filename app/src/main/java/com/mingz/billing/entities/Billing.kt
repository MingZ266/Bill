package com.mingz.billing.entities

import android.content.Context
import androidx.annotation.IntRange
import com.mingz.billing.utils.*
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.reflect.KProperty

abstract class Billing {
    companion object {
        private val dir by InitDir()
        @JvmField
        val billing = ArrayList<Billing>()

        @JvmStatic
        fun init(applicationContext: Context) {
            InitDir.init(applicationContext)
        }

        @JvmStatic
        fun readMonthlyBill(context: Context, year: Int, @IntRange(from = 1, to = 12) month: Int) {
            val content = Tools.readFile(File(dir, getFileName(year, month))) ?: return
            val isCiphertext = Config(context)[Config.CFG_ENCRYPT_BOOLEAN, false]
            val data = if (isCiphertext) {
                Encryption.decryptToString(content)
            } else {
                String(content, StandardCharsets.UTF_8)
            }

        }

        private fun getFileName(year: Int, month: Int) =
            Tools.md5("$year-${month.toString().padStart(2, '0')}")
    }

    abstract val typeId: Int

    abstract val type: String

    // 尽可能减少空间占用
    abstract fun toStringData(): String

    private class InitDir {
        companion object {
            private lateinit var dir: File

            fun init(applicationContext: Context) {
                dir = applicationContext.getExternalFilesDir("") ?: applicationContext.filesDir
                dir = File(dir, Constant.billingDir)
                if (!(dir.exists() || dir.mkdirs())) {
                    MyLog("Billing").e("目录创建失败: ${dir.absolutePath}")
                }
            }
        }

        operator fun getValue(companion: Billing.Companion, property: KProperty<*>) = dir
    }
}