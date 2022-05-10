package com.mingz.billing.entities

import com.mingz.billing.utils.MyLog
import com.mingz.billing.utils.StringWithId
import com.mingz.billing.utils.Tools.Companion.appendStringToJson
import org.json.JSONArray

class Expenditure(
    // 时间
    private val time: Long,
    // 支出科目
    private val subject: StringWithId,
    // 支出账户
    private val account: StringWithId,
    // 货币类型
    private val type: StringWithId,
    // 金额
    private val price: String,
    // 原价
    private val originalPrice: String,
    // 优惠
    private val discount: String,
    // 备注
    private val remarks: String
) : Billing() {

    companion object {
        const val typeId = 1
        const val typeDesc = "支出"
        private val myLog by lazy { MyLog("Expenditure") }

        @JvmStatic
        fun fromStringData(jsonArray: JSONArray): Expenditure? {
            myLog.v("解析支出: $jsonArray")
            return try {
                Expenditure(
                    jsonArray.getLong(0),
                    StringWithId(jsonArray.getInt(1), jsonArray.getString(2)),
                    StringWithId(jsonArray.getInt(3), jsonArray.getString(4)),
                    StringWithId(jsonArray.getInt(5), jsonArray.getString(6)),
                    jsonArray.getString(7),
                    jsonArray.getString(8),
                    jsonArray.getString(9),
                    jsonArray.getString(10)
                )
            } catch (e: Exception) {
                myLog.e("支出数据解析失败", e)
                null
            }
        }
    }

    override val typeId = Expenditure.typeId
    override val typeDesc = Expenditure.typeDesc
    override val timestamp = time

    // [time, subjectId, subjectContent, accountId, accountContent,
    //  typeId, typeContent, price, originalPrice, discount, remarks]
    // [Long, Int, String, Int, String,
    //  Int, String, String, String, String, String]
    override fun toJsonArray(): String {
        val cache = StringBuilder()
        cache.append('[')
        cache.append(time)
        cache.append(',')
        cache.append(subject.id)
        cache.append(',')
        cache.append('"').appendStringToJson(subject.content).append('"')
        cache.append(',')
        cache.append(account.id)
        cache.append(',')
        cache.append('"').appendStringToJson(account.content).append('"')
        cache.append(',')
        cache.append(type.id)
        cache.append(',')
        cache.append('"').appendStringToJson(type.content).append('"')
        cache.append(',')
        cache.append('"').append(price).append('"')
        cache.append(',')
        cache.append('"').append(originalPrice).append('"')
        cache.append(',')
        cache.append('"').append(discount).append('"')
        cache.append(',')
        cache.append('"').appendStringToJson(remarks).append('"')
        cache.append(']')
        myLog.v("保存支出: $cache")
        return cache.toString()
    }

    override fun toString(): String {
        return "Expenditure{ " +
                "time: $time, " +
                "subject: ${subject.id}、${subject.content}, " +
                "account: ${account.id}、${account.content}, " +
                "type: ${type.id}、${type.content}, " +
                "price: $price, " +
                "originalPrice: $originalPrice, " +
                "discount: $discount, " +
                "remarks: $remarks " +
                "}"
    }
}