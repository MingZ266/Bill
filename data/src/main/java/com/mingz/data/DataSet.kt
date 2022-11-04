// 科目主分类与账户属性反序列化解析有部分代码相似
@file:Suppress("DuplicatedCode")

package com.mingz.data

import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Xml
import com.mingz.data.bill.Bill
import com.mingz.share.AES_MODE
import com.mingz.share.MyLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.Collections
import javax.crypto.Cipher

/**
 * 支出科目（可能为空集合）.
 *
 * 由高度为2的无序树组成的森林，对应的有序数组为[orderedSubjectOutSet].
 * 不应在外部调用中修改其内部的值.
 *
 * 应保持同步的对象：[nextSubjectOutId]、[orderedSubjectOutSet].
 */
lateinit var subjectOutSet: Array<Subject>
    private set

/**
 * 收入科目（可能为空集合）.
 *
 * 由高度为2的无序树组成的森林，对应的有序数组为[orderedSubjectInSet].
 * 不应在外部调用中修改其内部的值.
 *
 * 应保持同步的对象：[nextSubjectInId]、[orderedSubjectInSet].
 */
lateinit var subjectInSet: Array<Subject>
    private set

/**
 * 账户（可能为空集合）.
 *
 * 按[Account.id]升序排列，不应在外部调用中修改其内部的值.
 *
 * 应保持同步的对象：[nextAccountId].
 */
lateinit var accountSet: Array<Account>
    private set

/**
 * 币种（可能为空集合）.
 *
 * 按[Type.id]升序排列，不应在外部调用中修改其内部的值.
 *
 * 应保持同步的对象：[nextTypeId].
 */
lateinit var typeSet: Array<Type>
    private set

/**
 * 按[Subject.id]升序排列的[subjectOutSet]的有序副本.
 */
private lateinit var orderedSubjectOutSet: Array<SubjectPack>

/**
 * 按[Subject.id]升序排列的[subjectInSet]的有序副本.
 */
private lateinit var orderedSubjectInSet: Array<SubjectPack>

private val myLog by lazy(LazyThreadSafetyMode.NONE) {
    MyLog("DataSetKt", false)
}
private val encoding = StandardCharsets.UTF_8
// 宏变量
private const val ERR_ID = Bill.NULL_ID
private const val ERR_NAME = "Null"
private const val ERR_COUNT = 0
private const val ERR_VAL = "0.00"
private const val START_ID = 1
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
private const val FILE_DATA_SET_DIR = "data_base" // 基础数据集外部存储files下存储目录
private const val FILE_SUBJECT_OUT = "data1.dat" // 支出科目数据集文件名
private const val FILE_SUBJECT_IN = "data2.dat" // 收入科目数据集文件名
private const val FILE_ACCOUNT = "data3.dat" // 账户数据集文件名
private const val FILE_TYPE = "data4.dat" // 币种数据集文件名

// 数据集文件
private val subjectOutFile = FilePack("$FILE_DATA_SET_DIR/$FILE_SUBJECT_OUT")
private val subjectInFile = FilePack("$FILE_DATA_SET_DIR/$FILE_SUBJECT_IN")
private val accountFile = FilePack("$FILE_DATA_SET_DIR/$FILE_ACCOUNT")
private val typeFile = FilePack("$FILE_DATA_SET_DIR/$FILE_TYPE")

// 记录下一个id
private var nextSubjectOutId = ERR_ID
private var nextSubjectInId = ERR_ID
private var nextAccountId = ERR_ID
private var nextTypeId = ERR_ID

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
            // 支出科目
            readDataSet(subjectOutFile, cipher, { parsingSubjectSet(it, false) }, {
                // 使用默认数据集
                readDefaultSubjectSet(applicationContext, false)
                saveSubjectOutSet()
            }, { // 读取或解析异常
                subjectOutSet = emptyArray()
                nextSubjectOutId = START_ID
                orderedSubjectOutSet = emptyArray()
            })
            // 收入科目
            readDataSet(subjectInFile, cipher, { parsingSubjectSet(it, true) }, {
                // 使用默认数据集
                readDefaultSubjectSet(applicationContext, true)
                saveSubjectInSet()
            }, { // 读取或解析异常
                subjectInSet = emptyArray()
                nextSubjectInId = START_ID
                orderedSubjectInSet = emptyArray()
            })
            // 账户
            readDataSet(accountFile, cipher, { parsingAccountSet(it) }, {
                accountSet = emptyArray() // 默认为空数据集
                nextAccountId = START_ID
                //saveAccountSet() // 默认数据集为空，不保存
            }, { // 读取或解析异常
                accountSet = emptyArray()
                nextAccountId = START_ID
            })
            // 币种
            readDataSet(typeFile, cipher, { parsingTypeSet(it) }, {
                // 使用默认数据集
                val arr = applicationContext.resources.getStringArray(R.array.defaultType)
                typeSet = Array(arr.size) { Type(it + 1, arr[it]) }
                nextTypeId = arr.size + 1
                saveTypeSet()
            }, { // 读取或解析异常
                typeSet = emptyArray()
                nextTypeId = START_ID
            })
        } catch (e: Exception) { // 保证初始化但不保存
            // 支出科目
            subjectOutSet = emptyArray()
            nextSubjectOutId = START_ID
            orderedSubjectOutSet = emptyArray()
            // 收入科目
            subjectInSet = emptyArray()
            nextSubjectInId = START_ID
            orderedSubjectInSet = emptyArray()
            // 账户
            accountSet = emptyArray()
            nextAccountId = START_ID
            // 币种
            typeSet = emptyArray()
            nextTypeId = START_ID
            myLog.w("初始化数据集失败", e, true)
        }
    }
    myLog.v("初始化基础数据集耗时${System.currentTimeMillis() - time}ms")
}

// 在[onData]中将字节数据格式化为数据集，若发生异常则会转到[onException]以避免影响后续数据集的读取
private inline fun readDataSet(
    filePack: FilePack, cipher: Cipher, onData: (ByteArray) -> Unit,
    onNothing: () -> Unit, onException: () -> Unit
) {
    try {
        onData(cipher.doFinal(FileInputStream(filePack.file).use { it.readBytes() }))
    } catch (e: FileNotFoundException) { // 数据文件不存在
        onNothing()
    } catch (e: Exception) {
        myLog.w("数据集读取失败", e)
        onException()
    }
}

/**
 * 读取默认的科目数据集.
 * @param isIncome 是否为收入科目
 */
private fun readDefaultSubjectSet(context: Context, isIncome: Boolean) {
    val subjectArray = context.resources.getStringArray(
        if (isIncome) R.array.defaultSubjectIn else R.array.defaultSubjectOut
    )
    val subjectList = ArrayList<Subject>() // 默认科目数据集
    var nextId = START_ID // 下一个id
    val orderedSubjectList = ArrayList<SubjectPack>()
    var mainId = ERR_ID // 主分类id
    var mainName: String? = null // 主分类名称
    val mainVices = ArrayList<Subject>() // 当前主分类下的所有副分类
    var viceAmount = -1 // 当前主分类下副分类数量
    var viceCount = 0 // 计数已经添加的副分类数量
    for (e in subjectArray) {
        if (mainName == null) { // 主分类
            mainId = nextId++
            mainName = e
        } else if (viceAmount < 0) { // 副分类数量
            viceAmount = try { e.toInt() } catch (e: Exception) {
                myLog.i("解析主分类($mainName)下副分类数量异常，其值为: $e")
                0
            }
            if (viceAmount <= 0) { // 该主分类下没有副分类
                // 添加主分类
                val main = Subject(mainId, mainName, emptyArray())
                subjectList.add(main)
                orderedSubjectList.add(SubjectPack(main)) // 默认数据集分配递增的id值
                // 重置
                mainId = ERR_ID
                mainName = null
                viceAmount = -1
            }
        } else { // 副分类，且[viceAmount]至少为1
            // 添加副分类
            mainVices.add(Subject(nextId++, e))
            if (++viceCount >= viceAmount) { // 已添加所有副分类
                // 添加主分类
                val main = Subject(mainId, mainName, mainVices.toArray(emptyArray<Subject>()))
                subjectList.add(main)
                // 默认数据集先为主分类分配id值，再为其下的副分类分配id值
                orderedSubjectList.add(SubjectPack(main))
                for (vice in main.allVice!!) {
                    orderedSubjectList.add(SubjectPack(vice, main))
                }
                // 重置
                mainId = ERR_ID
                mainName = null
                mainVices.clear()
                viceAmount = -1
                viceCount = 0
            }
        }
    }
    if (mainName != null) {
        myLog.d("默认的科目数据集中最后一个主分类的副分类数量错误。")
    }
    val subjectSet = subjectList.toArray(emptyArray<Subject>())
    val orderedSubjectSet = orderedSubjectList.toArray(emptyArray<SubjectPack>())
    if (isIncome) {
        subjectInSet = subjectSet
        nextSubjectInId = nextId
        orderedSubjectInSet = orderedSubjectSet
    } else {
        subjectOutSet = subjectSet
        nextSubjectOutId = nextId
        orderedSubjectOutSet = orderedSubjectSet
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
private suspend inline fun saveDataSet(filePack: FilePack,
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
            myLog.w("数据集存储失败", e)
        }
    }
}

/**
 * 将[subjectOutSet]或[subjectInSet]转为字节数据.
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
 * 将[accountSet]转为字节数据.
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
 * 将[typeSet]转为字节数据.
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
 * 从字节数据中解析出[subjectOutSet]或[subjectInSet].
 * @param isIncome 是否为收入科目
 * @see getSubjectSetBytes
 */
private fun parsingSubjectSet(data: ByteArray, isIncome: Boolean) {
    val xml = Xml.newPullParser()
    val encodingName = encoding.name()
    return ByteArrayInputStream(data).use { bis ->
        val subjectList = ArrayList<Subject>()
        var nextId = ERR_ID // 记录下一个id
        val orderedSubjectList = ArrayList<SubjectPack>()
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
                            // 记录可能的下一个id
                            if (nextId <= mainId) {
                                nextId = mainId + 1
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
                            // 记录可能的下一个id
                            if (nextId <= id) {
                                nextId = id + 1
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
                            val main = Subject(mainId, mainName,
                                mainVices.toArray(emptyArray<Subject>()), mainCount)
                            subjectList.add(main)
                            // 二分法插入有序表
                            for (i in -1 until main.allVice!!.size) {
                                val pack = if (i < 0) {
                                    SubjectPack(main)
                                } else {
                                    SubjectPack(main.allVice[i], main)
                                }
                                // 定位插入位置
                                val index = Collections.binarySearch(orderedSubjectList, pack)
                                if (index < 0) {
                                    orderedSubjectList.add(- index - 1/*换算为插入位置*/, pack)
                                } else {
                                    myLog.d("科目id重复:")
                                    myLog.d("    保留项: ${orderedSubjectList[index].subject}")
                                    myLog.d("    丢弃项: ${pack.subject}")
                                }
                            }
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
        val subjectSet = subjectList.toArray(emptyArray<Subject>())
        val orderedSubjectSet = orderedSubjectList.toArray(emptyArray<SubjectPack>())
        if (isIncome) {
            subjectInSet = subjectSet
            nextSubjectInId = nextId
            orderedSubjectInSet = orderedSubjectSet
        } else {
            subjectOutSet = subjectSet
            nextSubjectOutId = nextId
            orderedSubjectOutSet = orderedSubjectSet
        }
    }
}

/**
 * 从字节数据中解析出[accountSet].
 * @see getAccountSetBytes
 */
private fun parsingAccountSet(data: ByteArray) {
    val xml = Xml.newPullParser()
    val encodingName = encoding.name()
    ByteArrayInputStream(data).use { bis ->
        val accountList = ArrayList<Account>() // 账户数据集
        // 指示解析的结果是否按id值升序排列，通常为是
        var ordered = true
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
                            // 记录可能的下一个id
                            if (nextAccountId <= accountId) {
                                nextAccountId = accountId + 1
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
                            val account = Account(accountId, accountName,
                                assets.toArray(emptyArray<Asset>()), accountCount)
                            if (accountList.isNotEmpty() && ordered) {
                                // 比较即将添加的数据与上一个数据以判定是否升序
                                ordered = accountList[accountList.lastIndex].id <= account.id
                            }
                            accountList.add(account)
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
        if (!ordered) {
            myLog.d("账户数据集存储乱序")
            accountSet.sort()
        }
    }
}

/**
 * 从字节数据中解析出[typeSet].
 * @see getTypeSetBytes
 */
private fun parsingTypeSet(data: ByteArray) {
    ByteArrayInputStream(data).use { bis ->
        InputStreamReader(bis, encoding).use { isr ->
            JsonReader(isr).use { jr ->
                val typeList = ArrayList<Type>() // 币种数据集
                // 指示解析的结果是否按id值升序排列，通常为是
                var ordered = true
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
                    // 记录可能的下一个id
                    if (nextTypeId <= id) {
                        nextTypeId = id + 1
                    }
                    // 添加币种
                    val type = Type(id, name)
                    if (typeList.isNotEmpty() && ordered) {
                        // 比较即将添加的数据与上一个数据以判定是否升序
                        ordered = typeList[typeList.lastIndex].id <= type.id
                    }
                    typeList.add(type)
                    jr.endObject()
                }
                jr.endArray()
                typeSet = typeList.toArray(emptyArray<Type>())
                if (!ordered) {
                    myLog.d("币种数据集存储乱序")
                    Arrays.sort(typeSet)
                }
            }
        }
    }
}

/**
 * 通过[Subject.id]查找支出科目.
 */
fun findSubjectOut(id: Int?) = find(orderedSubjectOutSet, id)

/**
 * 通过[Subject.id]查找收入科目.
 */
fun findSubjectIn(id: Int?) = find(orderedSubjectInSet, id)

/**
 * 通过[Account.id]查找账户.
 */
fun findAccount(id: Int?) = find(accountSet, id)

/**
 * 通过[Type.id]查找币种.
 */
fun findType(id: Int?) = find(typeSet, id)

// 按给定id值以二分法查找目标元素
private fun <T> find(arr: Array<T>, id: Int?): T? {
    if (id == null || id == Bill.NULL_ID) return null
    val index = Arrays.binarySearch(arr, id)
    return if (index < 0) null else arr[index]
}

/**
 * 对[Subject]类进行包装.
 * @param subject 所代表的科目
 * @param main 当[subject]为副分类时，该值为其所属的主分类，否则为空
 */
class SubjectPack(val subject: Subject, val main: Subject? = null) : Comparable<Any> {

    override fun compareTo(other: Any) = subject.compareTo(other)

    /**
     * 是否为主分类.
     */
    fun isMain() = main == null
}

// 基础数据集实体类
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
     * 若为空集合，则表示此为没有副分类的主分类.
     */
    val allVice: Array<Subject>? = null,

    /**
     * 有关账单数量.
     */
    count: Int = 0
) : Count(count), Comparable<Any> {
    override fun compareTo(other: Any) = when (other) {
        is Subject -> id.compareTo(other.id)
        is Int -> id.compareTo(other)
        is SubjectPack -> id.compareTo(other.subject.id)
        else -> throw ClassCastException("${javaClass.name}: 不是可以比较的类型(${other.javaClass.name})")
    }

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
) : Count(count), Comparable<Any> {
    override fun compareTo(other: Any) = when (other) {
        is Account -> id.compareTo(other.id)
        is Int -> id.compareTo(other)
        else -> throw ClassCastException("${javaClass.name}: 不是可以比较的类型(${other.javaClass.name})")
    }

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
) : Comparable<Any> {
    override fun compareTo(other: Any) = when (other) {
        is Type -> id.compareTo(other.id)
        is Int -> id.compareTo(other)
        else -> throw ClassCastException("${javaClass.name}: 不是可以比较的类型(${other.javaClass.name})")
    }

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
        } else {
            myLog.i("减少账单计数异常，当前计数: $count")
            myLog.i("发生计数异常的项为: $this")
        }
    }
}