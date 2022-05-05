package com.mingz.billing.entities

import com.mingz.billing.utils.StringWithId

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

        @JvmStatic
        fun fromStringData(data: String): Expenditure? {
            TODO("逆向解析")
        }
    }

    override val typeId = Expenditure.typeId
    override val typeDesc = Expenditure.typeDesc
    override val timestamp = time

    override fun toStringData(): String {
        val cache = StringBuilder()
        TODO("Not yet implemented")
    }
}