package com.mingz.data.bill

/**
 * 收入.
 */
class Income(
    /**
     * 收入科目id.
     */
    val subject: Int,

    /**
     * 收款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 收入时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = -1
) : Bill(2, id, time)