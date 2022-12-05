package com.mingz.security.option

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.mingz.security.*
import com.mingz.share.MyLog
import com.mingz.share.catchException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.SecretKey

/**
 * 使用字节流作为保护项的安全项.
 *
 * 通过密钥对保护安全密钥，使用保护项保护密钥对中的私钥，使得安全密钥更新时无需验证安全项.
 */
abstract class ByteSafetyOptionActivity : AppCompatActivity(), SafetyOption {

    /**
     * 保存安全密钥的文件名.
     */
    protected abstract val filename: String

    protected companion object {
        /**
         * 当安全密钥更新时更新安全密钥密文.
         */
        @JvmStatic
        fun whenSafeKeyUpdated(context: Context, safeKey: SecretKey, filename: String) {
            // 读取私钥密文和公钥
            val dataGroup = split(FileInputStream(safeKeyFile(context, filename)).use { it.readBytes() })
            dataGroup[0] = ByteArray(0) // 旧的安全密钥密文（释放存储）
            val privateKey = dataGroup[1] // 私钥密文
            val publicKey = dataGroup[2] // 公钥
            // 使用公钥加密安全密钥
            val ciphertext = RSA.encrypt(RSA.generatePublic(publicKey), safeKey.encoded)
            // 保存
            save(context, filename, ciphertext, privateKey, publicKey)
        }

        // 保存安全密钥密文、私钥密文、公钥
        private fun save(context: Context, filename: String,
                 safeKey: ByteArray, privateKey: ByteArray, publicKey: ByteArray) {
            val file = safeKeyFile(context, filename)
            if (file.exists() || file.createNewFile()) {
                FileOutputStream(file).use { it.write(combine(safeKey, privateKey, publicKey)) }
            }
        }

        /**
         * 验证保护项，进而解密安全密钥.
         * @param protection 待验证的保护项
         * @return 若保护项正确，则返回解密后的安全密钥，否则返回null
         */
        @JvmStatic
        suspend fun decryptSafeKey(context: Context, filename: String, protection: ByteArray): ByteArray? {
            // 拆分保存的数据
            val dataGroup = split(withContext(Dispatchers.IO) {
                FileInputStream(safeKeyFile(context, filename)).use { it.readBytes() }
            })
            val publicKey = RSA.generatePublic(dataGroup[2]) // 公钥
            catchException({
                // 尝试解密私钥，若解密失败则意味着保护项错误
                val privateKey = RSA.generatePrivate(decryptPrivateKey(protection, dataGroup[1]/*私钥密文*/))
                // 验证私钥是否正确，继而验证保护项是否正确
                verifyPrivateKey(privateKey, publicKey)
                // 使用私钥解密安全密钥
                return RSA.decrypt(privateKey, dataGroup[0]/*安全密钥密文*/)
            }, {
                MyLog().v(it, true)
            })
            return null
        }

        /**
         * 解密私钥.
         * @param ciphertext 使用保护项加密后的私钥密文
         */
        private fun decryptPrivateKey(protection: ByteArray, ciphertext: ByteArray) = AES.decrypt(
            // 将保护项使用SHA-256摘要后作为AES密钥解密私钥
            AES.generateKey(sha256(protection)), ciphertext
        )

        /**
         * 随机生成一串数据，通过加解密验证私钥是否正确.
         * @throws Exception 如果私钥错误
         */
        private fun verifyPrivateKey(privateKey: PrivateKey, publicKey: PublicKey) {
            val verify = SecureRandom().generateSeed(RSA.MAX_ENCRYPT)
            val result = RSA.decrypt(privateKey, RSA.encrypt(publicKey, verify))
            if (result.size != verify.size) throw Exception("私钥错误")
            for(i in result.indices) {
                if (result[i] != verify[i]) {
                    throw Exception("私钥错误")
                }
            }
        }
    }

    // 加密私钥
    private fun encryptPrivateKey(protection: ByteArray, privateKey: ByteArray) = AES.encrypt(
        // 将保护项使用SHA-256摘要后作为AES密钥加密私钥
        AES.generateKey(sha256(protection)), privateKey
    )

    /**
     * 将当前安全项的配置值写为[value].
     */
    protected abstract fun writeConfig(value: Boolean)

    /**
     * 请求启用安全项.
     * @param protection 保护项
     */
    protected suspend fun requestEnable(context: Context, protection: ByteArray) {
        // 生成用于保护安全密钥的密钥对
        val keyPair = RSA.generateKeyPair()
        val privateKey = keyPair.private.encoded
        // 加密私钥
        val ciphertext = encryptPrivateKey(protection, privateKey)
        // 保存加密后的私钥和公钥
        withContext(Dispatchers.IO) {
            // 暂写入空的安全密钥密文，在更新时再写入
            save(context, filename, ByteArray(0), ciphertext, keyPair.public.encoded)
        }
        // 更新安全密钥
        SafeKey.update(context, generateSafeKey())
        // 写配置：安全项已启用
        writeConfig(true)
    }

    /**
     * 请求禁用安全项.
     *
     * 若保护项正确，将在配置文件中禁用安全项，且删除安全密钥密文存储文件.
     * @param protection 待验证的保护项
     * @param saveSafeKey 是否需要保存安全密钥明文，若该安全项为最后一项启用的安全项则需要保存
     * @return 保护项是否正确
     */
    protected suspend fun requestDisable(context: Context, protection: ByteArray, saveSafeKey: Boolean): Boolean {
        // 拆分保存的数据
        val dataGroup = split(withContext(Dispatchers.IO) {
            FileInputStream(safeKeyFile(context, filename)).use { it.readBytes() }
        })
        if (!saveSafeKey) dataGroup[0] = ByteArray(0) // 安全密钥密文（释放存储）
        val publicKey = RSA.generatePublic(dataGroup[2]) // 公钥
        catchException({
            // 尝试解密私钥，若解密失败则意味着保护项错误
            val privateKey = RSA.generatePrivate(decryptPrivateKey(protection, dataGroup[1]/*私钥密文*/))
            // 验证私钥是否正确，继而验证保护项是否正确
            verifyPrivateKey(privateKey, publicKey)
            // 验证成功
            catchException(MyLog()) {
                if (saveSafeKey) { // 保存安全密钥明文
                    saveSafeKey(context, RSA.decrypt(privateKey, dataGroup[0]))
                }
                // 写配置：安全项已禁用
                writeConfig(false)
                // 删除安全密钥密文存储文件
                safeKeyFile(context, filename).delete()
            }
            return true
        }, {
            MyLog().v(it, true)
        })
        return false
    }

    /**
     * 请求修改保护项.
     * @param oldProtection 旧的保护项
     * @param newProtection 新的保护项
     * @return 旧的保护项是否正确
     */
    protected suspend fun requestAlter(context: Context, oldProtection: ByteArray, newProtection: ByteArray): Boolean {
        // 验证旧的保护项是否正确
        val dataGroup = split(withContext(Dispatchers.IO) {
            FileInputStream(safeKeyFile(context, filename)).use { it.readBytes() }
        })
        val publicKey = RSA.generatePublic(dataGroup[2]) // 公钥
        val privateKey: ByteArray // 解密后的私钥
        try {
            privateKey = decryptPrivateKey(oldProtection, dataGroup[1]/*私钥密文*/)
            verifyPrivateKey(RSA.generatePrivate(privateKey), publicKey)
        } catch (e: Exception) { // 密码错误
            MyLog().v(e, true)
            return false
        }
        // 使用新的保护项加密私钥后保存
        dataGroup[1] = encryptPrivateKey(newProtection, privateKey)
        save(context, filename, dataGroup[0]/*安全密钥密文*/, dataGroup[1], dataGroup[2])
        return true
    }
}