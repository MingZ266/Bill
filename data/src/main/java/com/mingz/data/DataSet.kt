// 科目主分类与账户属性反序列化解析有部分代码相似
@file:Suppress("DuplicatedCode")

package com.mingz.data

import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Xml
import com.mingz.share.AES_MODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher

/**
 * 支出科目.
 */
lateinit var subjectOutSet: Array<Subject>
    private set

/**
 * 收入科目.
 */
lateinit var subjectInSet: Array<Subject>
    private set

/**
 * 账户.
 */
lateinit var accountSet: Array<Account>
    private set

/**
 * 币种.
 */
lateinit var typeSet: Array<Type>
    private set

private val encoding = StandardCharsets.UTF_8
// 宏变量
private const val ERR_ID = -1
private const val ERR_NAME = "Null"
private const val ERR_COUNT = 0
private const val ERR_VAL = "0.00"
// 节点
private const val ROOT = "root"
private const val MAIN = "main"
private const val VICE = "vice"
private const val ACCOUNT = "account"
private const val TYPE = "type"
// 属性
private const val ID = "id"
private const val NAME = "name"
private const val COUNT = "count"
private const val INIT_VAL = "initVal"
private const val NOW_VAL = "nowVal"
// 文件名
private const val FILE_DATA_SET_DIR = "base_data" // 基础数据集存储目录
private const val FILE_SUBJECT_OUT = "data1.dat" // 支出科目数据集文件名
private const val FILE_SUBJECT_IN = "data2.dat" // 收入科目数据集文件名
private const val FILE_ACCOUNT = "data3.dat" // 账户数据集文件名
private const val FILE_TYPE = "data4.dat" // 币种数据集文件名

// 数据集文件
private val subjectOutFile = InternalFilePack("$FILE_DATA_SET_DIR/$FILE_SUBJECT_OUT")
private val subjectInFile = InternalFilePack("$FILE_DATA_SET_DIR/$FILE_SUBJECT_IN")
private val accountFile = InternalFilePack("$FILE_DATA_SET_DIR/$FILE_ACCOUNT")
private val typeFile = InternalFilePack("$FILE_DATA_SET_DIR/$FILE_TYPE")

// TODO: 在获取safeKey后调用
/**
 * 初始化基础数据集.
 *
 * 只能在“安全访问”模块设置安全密钥后调用.
 */
suspend fun initDataSet(applicationContext: Context) {
    val time = System.currentTimeMillis()
    subjectOutFile.init(applicationContext)
    subjectInFile.init(applicationContext)
    accountFile.init(applicationContext)
    typeFile.init(applicationContext)
    withContext(Dispatchers.IO) {
        try {
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.DECRYPT_MODE, safeKey)
            // TODO: 文件不存在时使用默认数据集
            // 支出科目
            readDataSet(subjectOutFile, cipher, { subjectOutSet = parsingSubjectSet(it) }, {
            }, { subjectOutSet = emptyArray() })
            // 收入科目
            readDataSet(subjectInFile, cipher, { subjectInSet = parsingSubjectSet(it) }, {
            }, { subjectInSet = emptyArray() })
            // 账户
            readDataSet(accountFile, cipher, { parsingAccountSet(it) }, {
            }, { accountSet = emptyArray() })
            // 币种
            readDataSet(typeFile, cipher, { parsingTypeSet(it) }, {
            }, { typeSet = emptyArray() })
        } catch (e: Exception) {
            subjectOutSet = emptyArray()
            subjectInSet = emptyArray()
            accountSet = emptyArray()
            typeSet = emptyArray()
            // TODO: log
        }
    }
    println("初始化基础数据集耗时${System.currentTimeMillis() - time}ms")
}

// 在[onData]中将字节数据格式化为数据集，若发生异常则会转到[onException]以避免影响后续数据集的读取
private inline fun readDataSet(
    filePack: InternalFilePack, cipher: Cipher, onData: (ByteArray) -> Unit,
    onNothing: () -> Unit, onException: () -> Unit
) {
    try {
        onData(cipher.doFinal(FileInputStream(filePack.file).use { it.readBytes() }))
    } catch (e: FileNotFoundException) { // 数据文件不存在
        onNothing()
    } catch (e: Exception) {
        // TODO: log
        onException()
    }
}

/**
 * 保存支出科目数据集.
 *
 * 只有[initDataSet]被调用过才能调用该方法.
 */
suspend fun saveSubjectOutSet() = saveDataSet(subjectOutFile) {
    getSubjectSetBytes(subjectOutSet)
}

/**
 * 保存收入科目数据集.
 *
 * 只有[initDataSet]被调用过才能调用该方法.
 */
suspend fun saveSubjectInSet() = saveDataSet(subjectInFile) {
    getSubjectSetBytes(subjectInSet)
}

/**
 * 保存账户数据集.
 *
 * 只有[initDataSet]被调用过才能调用该方法.
 */
suspend fun saveAccountSet() = saveDataSet(accountFile) {
    getAccountSetBytes()
}

/**
 * 保存币种数据集.
 *
 * 只有[initDataSet]被调用过才能调用该方法.
 */
suspend fun saveTypeSet() = saveDataSet(typeFile) {
    getTypeSetBytes()
}

// 在[getData]中序列化数据集，以避免发生异常影响后续数据集的存储
private suspend inline fun saveDataSet(filePack: InternalFilePack,
                                       crossinline getData: () -> ByteArray) {
    withContext(Dispatchers.IO) {
        try {
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, safeKey)
            val data = cipher.doFinal(getData())
            with(filePack.file) {
                if (exists() || createNewFile()) {
                    FileOutputStream(this).use { it.write(data) }
                }
            }
        } catch (e: Exception) {
            // TODO: log
        }
    }
}

// TODO: 检查属性含有特殊字符时功能是否正常

/**
 * 将“科目”数据集转为字节数据.
 * @see parsingSubjectSet
 */
private fun getSubjectSetBytes(subjectSet: Array<Subject>): ByteArray {
    val xml = Xml.newSerializer()
    val encodingName = encoding.name()
    return ByteArrayOutputStream().use { bos ->
        xml.setOutput(bos, encodingName)
        xml.startDocument(encodingName, true)
        xml.startTag(null, ROOT)
        for (main in subjectSet) {
            // 主分类
            xml.startTag(null, MAIN)
            xml.attribute(null, ID, main.id.toString())
            xml.attribute(null, NAME, main.name)
            xml.attribute(null, COUNT, main.count.toString())
            if (main.allVice != null) {
                for (vice in main.allVice) {
                    // 副分类
                    xml.startTag(null, VICE)
                    xml.attribute(null, ID, vice.id.toString())
                    xml.attribute(null, NAME, vice.name)
                    xml.attribute(null, COUNT, vice.count.toString())
                    xml.endTag(null, VICE)
                }
            }
            xml.endTag(null, MAIN)
        }
        xml.endTag(null, ROOT)
        xml.endDocument()
        bos.toByteArray()
    }
}

/**
 * 将“账户”数据集转为字节数据.
 * @see parsingAccountSet
 */
private fun getAccountSetBytes(): ByteArray {
    val xml = Xml.newSerializer()
    val encodingName = encoding.name()
    return ByteArrayOutputStream().use { bos ->
        xml.setOutput(bos, encodingName)
        xml.startDocument(encodingName, true)
        xml.startTag(null, ROOT)
        for (a in accountSet) {
            // 账户信息
            xml.startTag(null, ACCOUNT)
            xml.attribute(null, ID, a.id.toString())
            xml.attribute(null, NAME, a.name)
            xml.attribute(null, COUNT, a.count.toString())
            for (asset in a.assets) {
                // 资产
                xml.startTag(null, TYPE)
                xml.attribute(null, ID, asset.id.toString())
                xml.attribute(null, INIT_VAL, asset.initVal)
                xml.attribute(null, NOW_VAL, asset.nowVal)
                xml.attribute(null, COUNT, asset.count.toString())
                xml.endTag(null, TYPE)
            }
            xml.endTag(null, ACCOUNT)
        }
        xml.endTag(null, ROOT)
        xml.endDocument()
        bos.toByteArray()
    }
}

/**
 * 将“币种”数据集转为字节数据.
 * @see parsingTypeSet
 */
private fun getTypeSetBytes(): ByteArray {
    return ByteArrayOutputStream().use { bos ->
        OutputStreamWriter(bos, encoding).use { osw ->
            JsonWriter(osw).use { jw ->
                jw.beginArray()
                for (t in typeSet) {
                    // 币种信息
                    jw.beginObject()
                    jw.name(ID).value(t.id)
                    jw.name(NAME).value(t.name)
                    jw.endObject()
                }
                jw.endArray()
            }
        }
        bos.toByteArray()
    }
}

/**
 * 从字节数据中解析出“科目”数据集.
 * @see getSubjectSetBytes
 */
private fun parsingSubjectSet(data: ByteArray): Array<Subject> {
    val xml = Xml.newPullParser()
    val encodingName = encoding.name()
    return ByteArrayInputStream(data).use { bis ->
        val subjectList = ArrayList<Subject>()
        xml.setInput(bis, encodingName)
        // 主分类信息
        var mainId = ERR_ID
        var mainName = ERR_NAME
        var mainCount = ERR_COUNT
        val mainVices = ArrayList<Subject>()
        var event: Int
        do {
            xml.next()
            event = xml.eventType
            when(event) {
                XmlPullParser.START_TAG -> {
                    when(xml.name) {
                        MAIN -> {
                            // 记录主分类信息
                            val len = xml.attributeCount
                            for (i in 0 until len) {
                                val value = xml.getAttributeValue(i)
                                when(xml.getAttributeName(i)) {
                                    ID -> mainId = value.toInt()
                                    NAME -> mainName = value
                                    COUNT -> mainCount = value.toInt()
                                }
                            }
                        }
                        VICE -> {
                            // 记录副分类信息
                            val len = xml.attributeCount
                            var id = ERR_ID
                            var name = ERR_NAME
                            var count = ERR_COUNT
                            for (i in 0 until len) {
                                val value = xml.getAttributeValue(i)
                                when(xml.getAttributeName(i)) {
                                    ID -> id = value.toInt()
                                    NAME -> name = value
                                    COUNT -> count = value.toInt()
                                }
                            }
                            // 添加副分类
                            mainVices.add(Subject(id, name, count = count))
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when(xml.name) {
                        MAIN -> {
                            // 当前主分类结束
                            subjectList.add(Subject(mainId, mainName,
                                mainVices.toArray(emptyArray<Subject>()), mainCount))
                            // 重置信息
                            mainId = ERR_ID
                            mainName = ERR_NAME
                            mainCount = ERR_COUNT
                            mainVices.clear()
                        }
                        ROOT -> break
                    }
                }
            }
        } while (event != XmlPullParser.END_DOCUMENT)
        subjectList.toArray(emptyArray<Subject>())
    }
}

/**
 * 从字节数据中解析出“账户”数据集.
 * @see getAccountSetBytes
 */
private fun parsingAccountSet(data: ByteArray) {
    val xml = Xml.newPullParser()
    val encodingName = encoding.name()
    ByteArrayInputStream(data).use { bis ->
        val accountList = ArrayList<Account>() // 账户数据集
        xml.setInput(bis, encodingName)
        // 账户信息
        var accountId = ERR_ID
        var accountName = ERR_NAME
        var accountCount = ERR_COUNT
        val assets = ArrayList<Asset>()
        var event: Int
        do {
            xml.next()
            event = xml.eventType
            when(event) {
                XmlPullParser.START_TAG -> {
                    when(xml.name) {
                        ACCOUNT -> {
                            // 记录账户信息
                            val len = xml.attributeCount
                            for (i in 0 until len) {
                                val value = xml.getAttributeValue(i)
                                when(xml.getAttributeName(i)) {
                                    ID -> accountId = value.toInt()
                                    NAME -> accountName = value
                                    COUNT -> accountCount = value.toInt()
                                }
                            }
                        }
                        TYPE -> {
                            // 记录资产信息
                            var id = ERR_ID
                            var initVal = ERR_VAL
                            var nowVal = ERR_VAL
                            var count = ERR_COUNT
                            val len = xml.attributeCount
                            for (i in 0 until len) {
                                val value = xml.getAttributeValue(i)
                                when(xml.getAttributeName(i)) {
                                    ID -> id = value.toInt()
                                    INIT_VAL -> initVal = value
                                    NOW_VAL -> nowVal = value
                                    COUNT -> count = value.toInt()
                                }
                            }
                            // 添加资产
                            assets.add(Asset(id, initVal, nowVal, count))
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when(xml.name) {
                        ACCOUNT -> {
                            // 当前账户结束
                            accountList.add(Account(accountId, accountName,
                                assets.toArray(emptyArray<Asset>()), accountCount))
                            // 重置信息
                            accountId = ERR_ID
                            accountName = ERR_NAME
                            accountCount = ERR_COUNT
                            assets.clear()
                        }
                        ROOT -> break
                    }
                }
            }
        } while (event != XmlPullParser.END_DOCUMENT)
        accountSet = accountList.toArray(emptyArray<Account>())
    }
}

/**
 * 从字节数据中解析出“币种”数据集.
 * @see getTypeSetBytes
 */
private fun parsingTypeSet(data: ByteArray) {
    ByteArrayInputStream(data).use { bis ->
        InputStreamReader(bis, encoding).use { isr ->
            JsonReader(isr).use { jr ->
                val typeList = ArrayList<Type>() // 币种数据集
                jr.beginArray()
                while (jr.hasNext()) {
                    jr.beginObject()
                    // 记录币种信息
                    var id = ERR_ID
                    var name = ERR_NAME
                    while (jr.hasNext()) {
                        when (jr.nextName()) {
                            ID -> id = jr.nextInt()
                            NAME -> name = jr.nextString()
                        }
                    }
                    // 添加币种
                    typeList.add(Type(id, name))
                    jr.endObject()
                }
                jr.endArray()
                typeSet = typeList.toArray(emptyArray<Type>())
            }
        }
    }
}

/**
 * 科目.
 */
class Subject(
    /**
     * 科目id.
     */
    val id: Int,

    /**
     * 科目名称短语.
     */
    val name: String,

    /**
     * 所有副科目.
     *
     * 若为空，则表示此为副分类，否则为主分类.
     */
    val allVice: Array<Subject>? = null,

    /**
     * 有关账单数量.
     */
    count: Int = 0
) : Count(count) {
    override fun toString(): String {
        if (allVice == null) { // 当前为副科目
            return "($id: $name, 相关账单数量: $count)"
        }
        // 当前为主科目
        return "科目($id: $name, 相关账单数量: $count, 副科目: ${allVice.contentToString()})"
    }
}

/**
 * 账户.
 */
class Account(
    /**
     * 账户id.
     */
    val id: Int,

    /**
     * 账户名称.
     */
    val name: String,

    /**
     * 账户下的所有资产.
     */
    val assets: Array<Asset>,

    /**
     * 有关账单数量.
     */
    count: Int = 0
) : Count(count) {
    override fun toString() = "账户($id: $name, 相关账单数量: $count, 账户资产: ${assets.contentToString()})"
}

/**
 * 币种.
 */
class Type(
    /**
     * 币种id.
     */
    val id: Int,

    /**
     * 币种名称.
     */
    val name: String
) {
    override fun toString() = "币种($id: $name)"
}

/**
 * 账户资产.
 */
class Asset(
    /**
     * 币种id.
     */
    val id: Int,

    /**
     * 该币种初始余额.
     */
    val initVal: String,

    /**
     * 该币种当前余额.
     */
    val nowVal: String,

    /**
     * 有关账单数量.
     */
    count: Int = 0
) : Count(count) {
    override fun toString() = "($id, 初值: $initVal, 现值: $nowVal, 相关账单数量: $count)"
}

/**
 * 统计相关账单数量.
 */
abstract class Count(count: Int) {
    /**
     * 有关该项的账单数量.
     */
    var count = count
        private set

    /**
     * 当有账单使用了该项时，应调用以增加计数.
     */
    fun increaseCount() {
        count++
    }

    /**
     * 当有账单移除了对该项的使用时，应调用以减少计数.
     */
    fun reduceCount() {
        if (count > 0) {
            count--
        } // TODO: else -> Log
    }
}