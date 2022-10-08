package com.mingz.data.bill

/**
 * 基金卖出.
 */
class FundSales(
    /**
     * 基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val fund: String,

    /**
     * 卖出时间.
     *
     * 格式为“yyyy-MM-dd HH:mm”.
     */
    val salesTime: String,

    /**
     * 确认净值.
     *
     * 精确到万分位.
     */
    val netVal: String,

    /**
     * 确认份额.
     *
     * 精确到百分位.
     */
    val amount: String,

    /**
     * 收款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实收金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 到账时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = -1
) : Bill(5, id, time)