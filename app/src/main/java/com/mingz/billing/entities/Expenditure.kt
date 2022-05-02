package com.mingz.billing.entities

import com.mingz.billing.utils.StringWithId

class Expenditure(
    // 时间
    private val timestamp: Long,
    // 支出科目
    private val subject: StringWithId,
    // 支出账户
    private val account: StringWithId,
    // 货币类型
    private val currencyType: StringWithId,
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
        @JvmStatic
        fun fromStringData(data: String): Expenditure {
            TODO()
        }
    }

    override val typeId = 1

    override val type = "支出"

    override fun toStringData(): String {
        val cache = StringBuilder()
        TODO("Not yet implemented")
    }
}