package com.mingz.billing.entities

import android.content.Context
import androidx.annotation.IntRange
import com.mingz.billing.utils.Constant
import com.mingz.billing.utils.Encryption
import com.mingz.billing.utils.MyLog
import com.mingz.billing.utils.Tools
import com.mingz.billing.utils.Tools.Companion.appendStringToJson
import org.json.JSONArray
import java.io.File
import kotlin.reflect.KProperty

abstract class Billing {
    companion object {
        private val myLog by lazy { MyLog("Billing") }
        private val dir by InitDir()

        @JvmStatic
        lateinit var monthBilling: MonthBilling
            private set

        @JvmStatic
        fun init(applicationContext: Context) = InitDir.init(applicationContext)

        @JvmStatic
        fun readMonthBilling(year: Int, @IntRange(from = 1, to = 12) month: Int) {
            MyLog.TEMP.v("读取${year}年${month}月的账单")
            if (this::monthBilling.isInitialized &&
                monthBilling.year == year && monthBilling.month == month) {
                MyLog.TEMP.v("当前读取的月账单即为目标月账单")
                return
            }
            monthBilling = MonthBilling(year, month)
            val dataGroup = (Tools.readFile(File(dir, getFileName(year, month))) ?: return).let {
                Encryption.decryptIfNeedAndToString(it)
            }
            try {
                val jsonArray = JSONArray(dataGroup)
                val len = jsonArray.length()
                for (i in 0 until len step 2) {
                    val typeId = jsonArray.getString(i).toInt()
                    val data = jsonArray.getString(i + 1)
                    generateBilling(typeId, data)?.let {
                        MyLog.TEMP.v(it)
                        monthBilling.add(it)
                    }
                }
            } catch (e: Exception) {
                myLog.w("${year}年${month}月账单读取失败", e)
            }
        }

        @JvmStatic
        fun saveBilling(year: Int, @IntRange(from = 1, to = 12) month: Int,
                        billing: Billing): Boolean {
            readMonthBilling(year, month)
            monthBilling.add(billing)
            val cache = StringBuilder()
            cache.append('[')
            // 从新到旧保存，避免或减少读取时add中的数组中间插入操作
            for (day in monthBilling) {
                for (data in day) {
                    cache.append('"').append(data.typeId).append('"')
                    cache.append(',')
                    cache.append('"').appendStringToJson(data.toStringData()).append('"')
                    cache.append(',')
                }
            }
            cache.deleteAt(cache.lastIndex)
            cache.append(']')
            MyLog.TEMP.v("保存内容: $cache")
            val data = Encryption.encryptIfNeed(cache.toString())
            return Tools.saveFile(File(dir, getFileName(year, month)).apply {
                MyLog.TEMP.v("保存路径: $absolutePath")
            }, data)
        }

        private fun getFileName(year: Int, month: Int) =
            Tools.md5("$year-${month.toString().padStart(2, '0')}")

        private fun generateBilling(typeId: Int, data: String): Billing? {
            return when (typeId) {
                Expenditure.typeId -> Expenditure.fromStringData(data)
                Income.typeId -> Income.fromStringData(data)
                Transfer.typeId -> Transfer.fromStringData(data)
                else -> null
            }
        }
    }

    /**
     * 账单类型id.
     *
     * · 请定义一个静态常量并引用它.
     *
     * · 必须保证唯一.
     */
    abstract val typeId: Int

    /**
     * 账单类型简短描述.
     *
     * · 请定义一个静态常量并引用它.
     */
    abstract val typeDesc: String

    /**
     * 该账单产生时的毫秒级时间戳.
     */
    abstract val timestamp: Long

    /**
     * 该账单保存时的可逆向解析的数据.
     *
     * · 尽可能减少空间占用.
     */
    abstract fun toStringData(): String

    /**
     * 以时间戳倒序排列，即由新到旧.
     */
    class MonthBilling(val year: Int, val month: Int) : ArrayList<DayBilling>() {
        private val oneDay = 86400000 // 一天时间的毫秒数

        fun add(billing: Billing) {
            val dayBilling = run {
                val startTimestamp = billing.timestamp - billing.timestamp % oneDay
                for (i in indices) {
                    var dayBilling = this[i]
                    // 定位所属的那天
                    if (startTimestamp < dayBilling.startTimestamp) {
                        continue
                    } else if (startTimestamp > dayBilling.startTimestamp) {
                        // 追加这天的记录
                        dayBilling = DayBilling(startTimestamp)
                        add(i, dayBilling)
                    }
                    return@run dayBilling
                }
                // 比已有的所有都要早 或 当前为空集
                val dayBilling = DayBilling(startTimestamp)
                add(dayBilling)
                return@run dayBilling
            }
            run {
                for (i in dayBilling.indices) {
                    if (billing.timestamp >= dayBilling[i].timestamp) {
                        dayBilling.add(i, billing)
                        return@run
                    }
                }
                dayBilling.add(billing)
            }
        }
    }

    /**
     * 以时间戳倒序排列，即由新到旧.
     */
    class DayBilling(val startTimestamp: Long) : ArrayList<Billing>()

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