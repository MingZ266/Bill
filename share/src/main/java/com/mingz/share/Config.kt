package com.mingz.share

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Config(context: Context, filename: String) {
    val configFile: SharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE)

    /**
     * 保存配置信息.
     *
     * 泛型[T]只接受[String]、[Boolean]、[Int]、[Float]、[Long]类型.
     * @throws IllegalArgumentException 如果不是可接受的类型
     */
    operator fun <T> set(config: String, value: T) {
        configFile.edit {
            when (value) {
                is String -> putString(config, value)
                is Boolean -> putBoolean(config, value)
                is Int -> putInt(config, value)
                is Float -> putFloat(config, value)
                is Long -> putLong(config, value)
                else -> throw IllegalArgumentException("参数类型错误：$value")
            }
        }
    }

    /**
     * 读取配置信息.
     *
     * 泛型[T]只接受[String]、[Boolean]、[Int]、[Float]、[Long]类型.
     *
     * 若参数[defaultVal]为空，将以[String]类型读取.
     * @throws IllegalArgumentException 如果不是可接受的类型
     * @throws ClassCastException 如果[config]项不为[defaultVal]同类型的配置
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(config: String, defaultVal: T? = null): T {
        if (defaultVal == null || defaultVal is String) {
            return configFile.getString(config, null) as T
        }
        return when (defaultVal) {
            is Boolean -> configFile.getBoolean(config, defaultVal)
            is Int -> configFile.getInt(config, defaultVal)
            is Float -> configFile.getFloat(config, defaultVal)
            is Long -> configFile.getLong(config, defaultVal)
            else -> throw IllegalArgumentException("参数类型错误：$defaultVal")
        } as T
    }
}