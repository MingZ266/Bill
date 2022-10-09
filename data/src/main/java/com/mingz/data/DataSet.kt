package com.mingz.data

import android.util.JsonReader
import android.util.JsonWriter
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

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

// 宏变量
private const val ERR_ID = -1
private const val ERR_NAME = "Null"
private const val ERR_COUNT = 0
private const val ERR_VAL = "0.00"
// 节点
private const val ROOT = "root"
private const val EXPENDITURE = "expenditure"
private const val INCOME = "income"
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

/**
 * 将基础数据集转为字节数据以便存储.
 * @see parsingDataSet
 */
fun getDataSetBytes(subjectOutSet: Array<Subject>, subjectInSet: Array<Subject>,
                    accountSet: Array<Account>, typeSet: Array<Type>): ByteArray {
    // 序列化
    val xml = Xml.newSerializer()
    val encoding = StandardCharsets.UTF_8
    val encodingName = encoding.name()
    // 科目
    val subject: ByteArray
    ByteArrayOutputStream().use { bos ->
        xml.setOutput(bos, encodingName)
        xml.startDocument(encodingName, true)
        xml.startTag(null, ROOT)
        // 支出科目
        subjectSetToXml(subjectOutSet, EXPENDITURE, xml)
        // 收入科目
        subjectSetToXml(subjectInSet, INCOME, xml)
        xml.endTag(null, ROOT)
        xml.endDocument()
        subject = bos.toByteArray()
    }
    // 账户
    val account: ByteArray
    ByteArrayOutputStream().use { bos ->
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
        account = bos.toByteArray()
    }
    // 币种
    val type: ByteArray
    ByteArrayOutputStream().use { bos ->
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
        type = bos.toByteArray()
    }
    // 以“数据长度、数据内容”形式拼合数据
    val data = ByteArray(Int.SIZE_BYTES * 3 + subject.size + account.size + type.size)
    // 科目
    var start = connectData(data, subject, 0)
    // 账户
    start = connectData(data, account, start)
    // 币种
    connectData(data, type, start)
    return data
}

private fun subjectSetToXml(subjectSet: Array<Subject>, root: String, xml: XmlSerializer) {
    xml.startTag(null, root)
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
    xml.endTag(null, root)
}

private fun connectData(data: ByteArray, set: ByteArray, start: Int): Int {
    // 写入数据长度
    // 小端存储（低字节写到低地址，高字节写到高地址）
    var size = set.size
    val loc = start + Int.SIZE_BYTES
    for (i in start until loc) {
        data[i] = size.toByte()
        size = size ushr 8
    }
    // 写入数据内容
    System.arraycopy(set, 0, data, loc, set.size)
    return (loc + set.size)
}

/**
 * 从字节数据中解析出基础数据集.
 * @see getDataSetBytes
 */
@Suppress("DuplicatedCode")
fun parsingDataSet(data: ByteArray) {
    // 拆分数据块
    // 科目
    var start = 0
    val subject = splitData(data, start)
    // 账户
    start += (Int.SIZE_BYTES + subject.size)
    val account = splitData(data, start)
    // 币种
    start += (Int.SIZE_BYTES + account.size)
    val type = splitData(data, start)
    // 反序列化
    val xml = Xml.newPullParser()
    val encoding = StandardCharsets.UTF_8
    val encodingName = encoding.name()
    // 科目
    ByteArrayInputStream(subject).use { bis ->
        val subjectOutList = ArrayList<Subject>() // 支出科目
        val subjectInList = ArrayList<Subject>() // 收入科目
        xml.setInput(bis, encodingName)
        var isExpenditure = false // 是否为支出科目
        var isIncome = false // 是否为收入科目
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
                        EXPENDITURE -> isExpenditure = true
                        INCOME -> isIncome = true
                    }
                }
                XmlPullParser.END_TAG -> {
                    when(xml.name) {
                        MAIN -> {
                            // 当前主分类结束
                            val s = Subject(mainId, mainName,
                                mainVices.toArray(emptyArray<Subject>()), mainCount)
                            if (isExpenditure) {
                                subjectOutList.add(s)
                            } else if (isIncome) {
                                subjectInList.add(s)
                            }
                            // 重置信息
                            mainId = ERR_ID
                            mainName = ERR_NAME
                            mainCount = ERR_COUNT
                            mainVices.clear()
                        }
                        EXPENDITURE -> isExpenditure = false
                        INCOME -> isIncome = false
                        ROOT -> break
                    }
                }
            }
        } while (event != XmlPullParser.END_DOCUMENT)
        subjectOutSet = subjectOutList.toArray(emptyArray<Subject>())
        subjectInSet = subjectInList.toArray(emptyArray<Subject>())
    }
    // 账户
    ByteArrayInputStream(account).use { bis ->
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
    // 币种
    ByteArrayInputStream(type).use { bis ->
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

private fun splitData(data: ByteArray, start: Int): ByteArray {
    // 读取数据长度
    var size = 0
    for (i in 0 until Int.SIZE_BYTES) {
        size = (data[start + i].toInt() and 0xFF) shl (8 * i) or size
    }
    val loc = start + Int.SIZE_BYTES
    // 读取数据内容
    val set = ByteArray(size)
    System.arraycopy(data, loc, set, 0, size)
    return set
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