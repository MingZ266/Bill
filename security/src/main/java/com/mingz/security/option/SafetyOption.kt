package com.mingz.security.option

/**
 * 安全项应使用密钥对，其中私钥需保证私密，公钥用于加密安全密钥.
 *
 * 实现该接口的类需提供如下方法以在安全密钥更新时更新安全密钥密文：
 *
 * Java:
 * -     public static void whenSafeKeyUpdated(Context, SecretKey)
 *
 * Kotlin:
 * -     companion object {
 * -         @JvmStatic
 * -         fun whenSafeKeyUpdated(Context, SecretKey): Unit
 * -     }
 */
interface SafetyOption