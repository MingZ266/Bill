package com.mingz.data.bill

/**
 * 基金买入.
 */
class FundPurchase(
    /**
     * 基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val fund: String,

    /**
     * 付款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实付金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 优惠金额.
     *
     * 精确到百分位.
     */
    val discount: String,

    /**
     * 确认日期.
     *
     * 格式为“yyyy-MM-dd”.
     */
    val confirmDate: String,

    /**
     * 确认净值.
     *
     * 精确到万分位.
     */
    val netVal: String,

    /**
     * 买入手续费.
     *
     * 精确到百分位.
     */
    val charges: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 付款购买时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = -1
) : Bill(4, id, time)