package com.mingz.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 当安全密钥更新时调用以更新账单相关数据库的密钥.
 */
suspend fun updateDBKey() {
    withContext(Dispatchers.IO) {
        // TODO: 更新账单相关数据库的密钥
    }
}