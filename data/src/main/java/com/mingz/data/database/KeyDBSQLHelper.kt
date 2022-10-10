package com.mingz.data.database

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class KeyDBSQLHelper(context: Context) : SQLiteOpenHelper(context, KEY_DB_NAME, null, KEY_DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $KEY_BILL_NAME (" +
                "`$KEY_BILL_MONTH` TEXT PRIMARY KEY," +
                "`$KEY_BILL_KEY` BLOB NOT NULL" +
                ")")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}