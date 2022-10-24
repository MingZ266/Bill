package com.mingz.data.bill

/**
 * 基金转换.
 */
class FundTransfer(
    /**
     * 转出基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val outFund: String,

    /**
     * 转出份额.
     *
     * 精确到百分位.
     */
    val outAmount: String,

    /**
     * 转出基金净值.
     *
     * 精确到万分位.
     */
    val outNetVal: String,

    /**
     * 总的手续费.
     *
     * 精确到百分位.
     */
    val charges: String,

    /**
     * 转入基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val inFund: String,

    /**
     * 转入份额.
     *
     * 精确到百分位.
     */
    val inAmount: String,

    /**
     * 转入基金净值.
     *
     * 精确到万分位.
     */
    val inNetVal: String,

    /**
     * 动账时间.
     *
     * 格式为“yyyy-MM-dd HH:mm”，若实际尾差为0，则为空，否则非空.
     */
    val timeForAccount: String?,

    /**
     * 动账账户id.
     *
     * 当基金转换有尾差时，使用该账户多退少补，此时非空，否则为空.
     */
    val account: Int?,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实际动账金额.
     *
     * 即实际尾差；精确到百分位；正值表示退款，负值表示补款.
     * 若实际尾差为0，则为空，否则非空.
     */
    val price: String?,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 转换时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = -1
) : Bill(7, id, time)