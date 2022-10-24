package com.mingz.data.database

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import java.io.File

/**
 * 月账单数据库.
 */
internal class MonthBillSQLHelper(context: Context, year: Int, month: Int) : SQLiteOpenHelper(
    context, getPath(context, year, month), null, MONTH_BILL_VERSION
) {
    companion object {
        private fun getPath(context: Context, year: Int, month: Int): String {
            // 数据库文件名称
            val name = (year * 100 + month).toString(16)
            val filesDir = context.getExternalFilesDir("") ?: context.filesDir
            return File(File(filesDir, MONTH_BILL_DIR), name).absolutePath
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 账单数据索引表
        db.execSQL("CREATE TABLE `$TABLE_INDEX` (" +
                "`$FIELD_TYPE_ID` INTEGER, " +
                "`$FIELD_DATA_ID` INTEGER, " +
                "`$FIELD_TIME` STRING NOT NULL, " +
                "PRIMARY KEY (`$FIELD_TYPE_ID`, `$FIELD_DATA_ID`)" +
                ")")
        // 支出
        db.execSQL("CREATE TABLE `$TABLE_EXPENDITURE` (" +
                "`$FIELD_ID` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`$FIELD_SUBJECT` INTEGER NOT NULL, " +
                "`$FIELD_ACCOUNT` INTEGER NOT NULL, " +
                "`$FIELD_TYPE` INTEGER NOT NULL, " +
                "`$FIELD_PRICE` STRING NOT NULL, " +
                "`$FIELD_DISCOUNT` STRING NOT NULL, " +
                "`$FIELD_REMARK` STRING" +
                ")")
        // 收入
        db.execSQL("CREATE TABLE `$TABLE_INCOME` (" +
                "`$FIELD_ID` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`$FIELD_SUBJECT` INTEGER NOT NULL, " +
                "`$FIELD_ACCOUNT` INTEGER NOT NULL, " +
                "`$FIELD_TYPE` INTEGER NOT NULL, " +
                "`$FIELD_PRICE` STRING NOT NULL, " +
                "`$FIELD_REMARK` STRING" +
                ")")
        // 转账
        db.execSQL("CREATE TABLE `$TABLE_TRANSFER` (" +
                "`$FIELD_ID` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`$FIELD_OUT_ACCOUNT` INTEGER NOT NULL, " +
                "`$FIELD_IN_ACCOUNT` INTEGER NOT NULL, " +
                "`$FIELD_TYPE` INTEGER NOT NULL, " +
                "`$FIELD_OUT_PRICE` STRING NOT NULL, " +
                "`$FIELD_CHARGES` STRING NOT NULL, " +
                "`$FIELD_REMARK` STRING" +
                ")")
        // 基金买入
        db.execSQL("CREATE TABLE `$TABLE_FUND_PURCHASE` (" +
                "`$FIELD_ID` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`$FIELD_FUND` STRING NOT NULL, " +
                "`$FIELD_ACCOUNT` INTEGER NOT NULL, " +
                "`$FIELD_TYPE` INTEGER NOT NULL, " +
                "`$FIELD_PRICE` STRING NOT NULL, " +
                "`$FIELD_DISCOUNT` STRING NOT NULL, " +
                "`$FIELD_CONFIRM_DATE` STRING NOT NULL, " +
                "`$FIELD_NET_VAL` STRING NOT NULL, " +
                "`$FIELD_CHARGES` STRING NOT NULL, " +
                "`$FIELD_REMARK` STRING" +
                ")")
        // 基金卖出
        db.execSQL("CREATE TABLE `$TABLE_FUND_SALES` (" +
                "`$FIELD_ID` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`$FIELD_FUND` STRING NOT NULL, " +
                "`$FIELD_SALES_TIME` STRING NOT NULL, " +
                "`$FIELD_NET_VAL` STRING NOT NULL, " +
                "`$FIELD_AMOUNT` STRING NOT NULL, " +
                "`$FIELD_ACCOUNT` INTEGER NOT NULL, " +
                "`$FIELD_TYPE` INTEGER NOT NULL, " +
                "`$FIELD_PRICE` STRING NOT NULL, " +
                "`$FIELD_REMARK` STRING" +
                ")")
        // 基金分红
        db.execSQL("CREATE TABLE `$TABLE_FUND_DIVIDEND` (" +
                "`$FIELD_ID` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`$FIELD_FUND` STRING NOT NULL, " +
                "`$FIELD_PRICE` STRING NOT NULL, " +
                "`$FIELD_REDO` INTEGER NOT NULL DEFAULT '0', " +
                "`$FIELD_ACCOUNT` INTEGER, " +
                "`$FIELD_TYPE` INTEGER NOT NULL, " +
                "`$FIELD_NET_VAL` STRING, " +
                "`$FIELD_REMARK` STRING" +
                ")")
        // 基金转换
        db.execSQL("CREATE TABLE `$TABLE_FUND_TRANSFER` (" +
                "`$FIELD_ID` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`$FIELD_OUT_FUND` STRING NOT NULL, " +
                "`$FIELD_OUT_AMOUNT` STRING NOT NULL, " +
                "`$FIELD_OUT_NET_VAL` STRING NOT NULL, " +
                "`$FIELD_CHARGES` STRING NOT NULL, " +
                "`$FIELD_IN_FUND` STRING NOT NULL, " +
                "`$FIELD_IN_AMOUNT` STRING NOT NULL, " +
                "`$FIELD_IN_NET_VAL` STRING NOT NULL, " +
                "`$FIELD_TIME_FOR_ACCOUNT` STRING, " +
                "`$FIELD_ACCOUNT` INTEGER, " +
                "`$FIELD_TYPE` INTEGER NOT NULL, " +
                "`$FIELD_PRICE` STRING, " +
                "`$FIELD_REMARK` STRING" +
                ")")

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}