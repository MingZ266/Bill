package com.mingz.data

/**
 * 科目.
 */
class Subject(
    /**
     * 科目id.
     */
    val id: Int,

    /**
     * 科目名称短语.
     */
    val name: String
) : Count() {
    override fun toString() = "科目($id: $name, 相关账单数量: $count)"
}

/**
 * 账户.
 */
class Account(
    /**
     * 账户id.
     */
    val id: Int,

    /**
     * 账户名称.
     */
    val name: String,

    /**
     * 账户下的所有资产.
     */
    val assets: Array<Asset>
) : Count() {
    override fun toString() = "账户($id: $name, 相关账单数量: $count, 账户资产: ${assets.contentToString()})"
}

/**
 * 币种.
 */
class Type(
    /**
     * 币种id.
     */
    val id: Int,

    /**
     * 币种名称.
     */
    val name: String
) {
    override fun toString() = "币种($id: $name)"
}

/**
 * 账户资产.
 */
class Asset(
    /**
     * 币种id.
     */
    val id: Int,

    /**
     * 该币种初始余额.
     */
    val initVal: String,

    /**
     * 该币种当前余额.
     */
    val nowVal: String
) : Count() {
    override fun toString() = "($id, 初值: $initVal, 现值: $nowVal, 相关账单数量: $count)"
}

/**
 * 统计相关账单数量.
 */
abstract class Count {
    /**
     * 有关该项的账单数量.
     */
    var count = 0
        private set

    /**
     * 当有账单使用了该项时，应调用以增加计数.
     */
    fun increaseCount() {
        count++
    }

    /**
     * 当有账单移除了对该项的使用时，应调用以减少计数.
     */
    fun reduceCount() {
        if (count > 0) {
            count--
        } // TODO: else -> Log
    }
}