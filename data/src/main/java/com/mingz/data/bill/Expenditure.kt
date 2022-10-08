package com.mingz.data.bill

/**
 * 支出.
 */
class Expenditure(
    /**
     * 支出科目id.
     */
    val subject: Int,

    /**
     * 付款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实际支出金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 优惠.
     *
     * 精确到百分位.
     */
    val discount: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 支出时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = -1
) : Bill(1, id, time)