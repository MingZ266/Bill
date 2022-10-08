package com.mingz.data.bill

/**
 * 转账.
 */
class Transfer(
    /**
     * 转出账户id.
     */
    val outAccount: Int,

    /**
     * 转入账户id.
     */
    val inAccount: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 转出金额.
     *
     * 精确到百分位.
     */
    val outPrice: String,

    /**
     * 手续费.
     *
     * 精确到百分位.
     */
    val charges: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 转账时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = -1
) : Bill(3, id, time)