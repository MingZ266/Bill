package com.mingz.data.bill

abstract class Bill(
    /**
     * 账单类型id.
     *
     * 由子类指定，各子类代表不同的账单类型，该值应不同.
     */
    val typeId: Int,

    /**
     * 账单id.
     *
     * 从数据库中生成或读取.
     */
    val dataId: Int,

    /**
     * 账单产生时间.
     *
     * 格式为“yyyy-MM-dd HH:mm”.
     */
    val time: String
) {
    override fun toString(): String {
        return "Bill($typeId - ${if (dataId < 0) "Null" else dataId} $time)"
    }
}