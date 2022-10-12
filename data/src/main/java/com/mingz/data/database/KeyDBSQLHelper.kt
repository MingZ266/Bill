package com.mingz.data.database

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

/**
 * 存储月账单数据库密钥的密钥数据库.
 */
internal class KeyDBSQLHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_KEY_DB,
    null, KEY_DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_BILL_KEY (" +
                "`$FIELD_MONTH` STRING PRIMARY KEY, " +
                "`$FIELD_KEY` BLOB NOT NULL" +
                ")")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}