package com.mingz.data.database

/**
 * 月账单数据库在外部存储files下的存储目录.
 */
internal const val MONTH_BILL_DIR = "data"

// 数据库字段

// ##### 密钥数据库 #####
/**
 * 数据库名称：密钥数据库.
 */
internal const val DATABASE_KEY_DB = "key_db"

// *** 月账单数据库密钥表 ***
/**
 * 表名：密钥数据库 - 月账单数据库密钥表.
 */
internal const val TABLE_BILL_KEY = "bill_key"

/**
 * 字段：月账单数据库密钥表 - 月份.
 *
 * 格式为“yyyyMM”.
 */
internal const val FIELD_MONTH = "month"

/**
 * 字段：月账单数据库密钥表 - AES密钥.
 */
internal const val FIELD_KEY = "key"

// ##### 月账单数据库 #####
/**
 * 字段：各类型账单 - 账单id.
 */
internal const val FIELD_ID = "id"

/**
 * 字段：支出/收入 - ?.
 *
 * 含义：
 * - 支出：支出科目id
 * - 收入：收入科目id
 */
internal const val FIELD_SUBJECT = "subject"

/**
 * 字段：支出/收入/基金买入/基金卖出/基金分红/基金转换 - ?.
 *
 * 含义：
 * - 支出：付款账户id
 * - 收入：收款账户id
 * - 基金买入：付款账户id
 * - 基金卖出：收款账户id
 * - 基金分红：收款账户id
 * - 基金转换：退补账户id
 */
internal const val FIELD_ACCOUNT = "account"

/**
 * 字段：各类型账单 - 币种id.
 */
internal const val FIELD_TYPE = "type"

/**
 * 字段：支出/收入/基金买入/基金卖出/基金分红/基金转换 - ?.
 *
 * 含义：
 * - 支出：支出金额
 * - 收入：收入金额
 * - 基金买入：实付金额
 * - 基金卖出：实收金额
 * - 基金分红：分红金额
 * - 基金转换：实际退补金额
 */
internal const val FIELD_PRICE = "price"

/**
 * 字段：支出/基金买入 - 优惠金额.
 */
internal const val FIELD_DISCOUNT = "discount"

/**
 * 字段：各类型账单 - 备注.
 */
internal const val FIELD_REMARK = "remark"

/**
 * 字段：转账/基金买入/基金转换 - 手续费.
 */
internal const val FIELD_CHARGES = "charges"

/**
 * 字段：基金买入/基金卖出/基金分红 - 基金.
 */
internal const val FIELD_FUND = "fund"

/**
 * 字段：基金买入/基金卖出/基金分红 - ?.
 *
 * 含义：
 * - 基金买入：确认净值
 * - 基金卖出：确认净值
 * - 基金分红：基金净值
 */
internal const val FIELD_NET_VAL = "net_val"

// *** 账单数据索引表 ***
/**
 * 表名：月账单数据库 - 账单数据索引表.
 */
internal const val TABLE_INDEX = "index"

/**
 * 字段：账单数据索引表 - 账单类型id.
 */
internal const val FIELD_TYPE_ID = "type_id"

/**
 * 字段：账单数据索引表 - 账单id.
 */
internal const val FIELD_DATA_ID = "data_id"

/**
 * 字段：账单数据索引表 - 账单产生时间.
 *
 * 含义：
 * - 支出：支出时间
 * - 收入：收入时间
 * - 转账：转账时间
 * - 基金买入：购买时间
 * - 基金卖出：到账时间
 * - 基金分红：分红时间
 * - 基金转换：转换时间
 */
internal const val FIELD_TIME = "time"

// *** 支出 ***
/**
 * 表名：月账单数据库 - 支出.
 */
internal const val TABLE_EXPENDITURE = "expenditure"

// *** 收入 ***
/**
 * 表名：月账单数据库 - 收入.
 */
internal const val TABLE_INCOME = "income"

// *** 转账 ***
/**
 * 表名：月账单数据库 - 转账.
 */
internal const val TABLE_TRANSFER = "transfer"

/**
 * 字段：转账 - 转出账户id.
 */
internal const val FIELD_OUT_ACCOUNT = "out_account"

/**
 * 字段：转账 - 转入账户id.
 */
internal const val FIELD_IN_ACCOUNT = "in_account"

/**
 * 字段：转账 - 转出金额.
 */
internal const val FIELD_OUT_PRICE = "out_price"

// *** 基金买入 ***
/**
 * 表名：月账单数据库 - 基金买入.
 */
internal const val TABLE_FUND_PURCHASE = "fund_purchase"

/**
 * 字段：基金买入 - 确认日期.
 */
internal const val FIELD_CONFIRM_DATE = "confirm_date"

// *** 基金卖出 ***
/**
 * 表名：月账单数据库 - 基金卖出.
 */
internal const val TABLE_FUND_SALES = "fund_sales"

/**
 * 字段：基金卖出 - 卖出时间.
 */
internal const val FIELD_SALES_TIME = "sales_time"

/**
 * 字段：基金卖出 - 确认份额.
 */
internal const val FIELD_AMOUNT = "amount"

// *** 基金分红 ***
/**
 * 表名：月账单数据库 - 基金分红.
 */
internal const val TABLE_FUND_DIVIDEND = "fund_dividend"

/**
 * 字段：基金分红 - 是否再投资.
 */
internal const val FIELD_REDO = "redo"

// *** 基金转换 ***
/**
 * 表名：月账单数据库 - 基金转换.
 */
internal const val TABLE_FUND_TRANSFER = "fund_transfer"

/**
 * 字段：基金转换 - 转出基金.
 */
internal const val FIELD_OUT_FUND = "out_fund"

/**
 * 字段：基金转换 - 转出份额.
 */
internal const val FIELD_OUT_AMOUNT = "out_amount"

/**
 * 字段：基金转换 - 转出基金净值.
 */
internal const val FIELD_OUT_NET_VAL = "out_net_val"

/**
 * 字段：基金转换 - 转入基金.
 */
internal const val FIELD_IN_FUND = "in_fund"

/**
 * 字段：基金转换 - 转入份额.
 */
internal const val FIELD_IN_AMOUNT = "in_amount"

/**
 * 字段：基金转换 - 转入基金净值.
 */
internal const val FIELD_IN_NET_VAL = "in_net_val"

/**
 * 字段：基金转换 - 动账时间.
 */
internal const val FIELD_TIME_FOR_ACCOUNT = "time_for_account"