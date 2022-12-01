package com.mingz.data

/**
 * @see SafeKeyProvider.safeKey
 */
internal val safeKey: ByteArray
    get() = provider.safeKey

/**
 * @see SafeKeyProvider.encrypt
 */
internal fun encrypt(data: ByteArray) = provider.encrypt(data)

/**
 * @see SafeKeyProvider.decrypt
 */
internal fun decrypt(ciphertext: ByteArray) = provider.decrypt(ciphertext)

/**
 * 当安全密钥更新时调用以更新存储的数据.
 */
suspend fun whenSafeKeyUpdated() {
    updateDataSetCiphertext()
    updateDBKey()
}

// 提供程序
private lateinit var provider: SafeKeyProvider

/**
 * 设置安全密钥提供程序.
 */
fun setSafeKeyProvider(safeKeyProvider: SafeKeyProvider) {
    provider = safeKeyProvider
}

/**
 * 安全密钥提供程序.
 */
interface SafeKeyProvider {
    /**
     * 安全密钥.
     *
     * 作为密钥数据库和基础数据集的加解密密钥.
     */
    val safeKey: ByteArray

    /**
     * 使用安全密钥加密[data]，返回加密结果.
     */
    fun encrypt(data: ByteArray): ByteArray

    /**
     * 使用安全密钥解密[ciphertext]，返回解密结果.
     */
    fun decrypt(ciphertext: ByteArray): ByteArray
}