package com.mingz.billing.utils

import android.content.Context
import androidx.annotation.StringDef
import androidx.core.content.edit

class Config(context: Context) {
    private val configFile = context.getSharedPreferences(Constant.configFile, Context.MODE_PRIVATE)

    companion object {
        const val CFG_ENCRYPT_BOOLEAN = "cfg_encrypt"
    }

    /**
     * 保存配置信息.
     *
     * 泛型[T]只接受[Int]、[Boolean]、[String]、[Long]、[Float]类型.
     * @throws IllegalArgumentException 如果不是可接受的类型
     */
    operator fun <T> set(@Config config: String, value: T) {
        configFile.edit {
            when (value) {
                is Int -> putInt(config, value)
                is Boolean -> putBoolean(config, value)
                is String -> putString(config, value)
                is Long -> putLong(config, value)
                is Float -> putFloat(config, value)
                else -> throw IllegalArgumentException("参数\"$value\"不是可接受的类型.")
            }
        }
    }

    /**
     * 读取指定的[String]类型配置.
     */
    operator fun get(@Config config: String, defaultVal: String? = null): String? {
        return configFile.getString(config, defaultVal)
    }

    /**
     * 读取指定的配置.
     *
     * 泛型[T]只接受[Int]、[Boolean]、[Long]、[Float]类型.
     * @throws IllegalArgumentException 如果不是可接受的类型
     * @throws ClassCastException 如果[config]项不为[defaultVal]同类型的配置
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(@Config config: String, defaultVal: T): T {
        return when (defaultVal) {
            is Int -> configFile.getInt(config, defaultVal)
            is Boolean -> configFile.getBoolean(config, defaultVal)
            is Long -> configFile.getLong(config, defaultVal)
            is Float -> configFile.getFloat(config, defaultVal)
            else -> throw IllegalArgumentException("参数\"$defaultVal\"不是可接受的类型.")
        } as T
    }

    @StringDef(CFG_ENCRYPT_BOOLEAN)
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    private annotation class Config
}