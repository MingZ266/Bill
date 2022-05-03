package com.mingz.billing.utils

class Constant {
    companion object {
        //******文件（夹）名******
        // 数据源存储文件，存储在外部存储files目录下
        @JvmField
        val dataSourceFile = Tools.md5("date source")

        // 账单数据目录，存储在外部存储files目录下
        @JvmField
        val billingDir = Tools.md5("billing")

        // 加密密钥存储目录，存储在内部存储files目录下
        @JvmField
        val encryptionDir = Tools.md5("encryption")

        // 配置文件名称，使用共享存储
        const val configFile = "config"

        //******键名******
        // 切换动画的类型
        const val KEY_ANIM_TYPE_INT = "anim_type"

        // 切换动画的方向
        const val KEY_ANIM_DIRECTION_INT = "anim_direction"
    }
}