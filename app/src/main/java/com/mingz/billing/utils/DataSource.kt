package com.mingz.billing.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import com.mingz.billing.R
import com.mingz.billing.ui.DrawableTextView
import com.mingz.billing.ui.MultilevelListView.Data
import com.mingz.billing.utils.Tools.Companion.appendStringToJson
import com.mingz.billing.utils.Tools.Companion.add
import com.mingz.billing.utils.Tools.Companion.remove
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KProperty

class DataSource private constructor() {

    companion object {
        private val myLog = MyLog(this)
        private val file by InitFile()

        /**
         * 所有账户.
         */
        @JvmField
        val accountList = AccountList()

        /**
         * 所有可用账户.
         */
        @JvmField
        val availableAccount = AccountList()

        /**
         * 所有货币类型.
         */
        @JvmField
        val typeList = StringList()

        /**
         * 所有基金.
         */
        @JvmField
        val fundList = FundList()

        /**
         * 支出科目.
         */
        @JvmField
        val expenditureSubject = SubjectArray()

        /**
         * 收入科目.
         */
        @JvmField
        val incomeSubject = SubjectArray()

        @JvmField
        var checkedPosition = -1

        @JvmStatic
        fun init(applicationContext: Context) = InitFile.init(applicationContext)

        @JvmStatic
        fun initData(context: Context) {
            clearDataSource()
            val json = Tools.readFile(file)?.let {
                Encryption.decryptIfNeedAndToString(it)
            }
            if (json == null) {
                initUseDefault(context)
            } else {
                try {
                    val jsonObj = JSONObject(json)
                    accountList.init(jsonObj.getJSONArray("ac"))
                    typeList.init(jsonObj.getJSONArray("ty"))
                    fundList.init(jsonObj.getJSONArray("fu"))
                    expenditureSubject.init(jsonObj.getJSONArray("ex"))
                    incomeSubject.init(jsonObj.getJSONArray("in"))
                } catch (e: Exception) {
                    myLog.w("数据源存储文件解析失败", e)
                    clearDataSource()
                    initUseDefault(context)
                }
            }
        }

        private fun clearDataSource() {
            accountList.clear()
            typeList.clear()
            fundList.clear()
            expenditureSubject.clear()
            incomeSubject.clear()
        }

        private fun initUseDefault(context: Context) {
            val res = context.resources
            typeList.init(res.getStringArray(R.array.defaultType))
            expenditureSubject.init(res.getStringArray(R.array.defaultExpenditure))
            incomeSubject.init(res.getStringArray(R.array.defaultIncome))
            // TODO: delete it
            if (myLog.debug) {
                accountList.add("账户A", arrayOf(Assets(typeList[0], "0.00", "2.00")))
                accountList.add("账户B", arrayOf(Assets(typeList[0], "0.00", "-6.00")))
                accountList.add("账户C", arrayOf(Assets(typeList[0], "0.00")))
                accountList.add("账户D", arrayOf(Assets(typeList[0], "0.00")))
                accountList.add("账户E", arrayOf(Assets(typeList[0], "0.00")))
                accountList.add("账户F", arrayOf(Assets(typeList[0], "0.00")))
                accountList.add("账户G", arrayOf(Assets(typeList[0], "0.00")))
                accountList.add("账户H", arrayOf(Assets(typeList[0], "0.00")))
                availableAccount.addAll(accountList)
                fundList.add("基金A", "001")
                fundList.add("基金B", "002")
                fundList.add("基金C", "003")
                fundList.add("基金D", "004")
                fundList.add("基金E", "005")
                fundList.add("基金F", "006")
            }
        }

        @JvmStatic
        fun save() {
            Tools.saveFile(file, Encryption.encryptIfNeed(toJson()))
        }

        private fun toJson(): String {
            val cache = StringBuilder()
            cache.append('{')
            cache.append("\"ac\":").append(accountList.toJsonArray())
            cache.append(',')
            cache.append("\"ty\":").append(typeList.toJsonArray())
            cache.append(',')
            cache.append("\"fu\":").append(fundList.toJsonArray())
            cache.append(',')
            cache.append("\"ex\":").append(expenditureSubject.toJsonArray())
            cache.append(',')
            cache.append("\"in\":").append(incomeSubject.toJsonArray())
            cache.append('}')
            return cache.toString()
        }
    }

    override fun toString() = toJson()

    private class InitFile {
        companion object {
            private lateinit var file: File

            fun init(applicationContext: Context) {
                val dir = applicationContext.getExternalFilesDir("")
                    ?: applicationContext.filesDir
                file = File(dir, Constant.dataSourceFile)
                if (!(dir.exists() || dir.mkdirs())) {
                    MyLog("DataSource").e("目录创建失败: ${dir.absolutePath}")
                }
            }
        }

        operator fun getValue(companion: DataSource.Companion, property: KProperty<*>) = file
    }
}

class StringWithId(val id: Int, val content: String) {
    fun setContent(content: String) = StringWithId(id, content)
}

abstract class MyArrayList<E> : ArrayList<E>() {
    protected var nextId = 1

    abstract fun init(jsonArr: JSONArray)

    fun floatToTop(index: Int) {
        val element = removeAt(index)
        add(0, element)
    }

    override fun clear() {
        super.clear()
        nextId = 1
    }

    abstract fun toJsonArray(): String

    override fun toString() = "nextId: $nextId; content: ${toJsonArray()}"
}

class StringList : MyArrayList<StringWithId>() {
    fun init(source: Array<String>) {
        for (data in source) {
            add(data)
        }
    }

    override fun init(jsonArr: JSONArray) {
        val len = jsonArr.length()
        for (i in 0 until len step 2) {
            val id = jsonArr.getInt(i)
            add(StringWithId(id, jsonArr.getString(i + 1)))
            if (nextId <= id) {
                nextId = id + 1
            }
        }
    }

    fun add(content: String) {
        add(StringWithId(nextId++, content))
    }

    fun copy(): StringList {
        val copy = StringList()
        copy.addAll(this)
        copy.nextId = nextId
        return copy
    }

    fun replace(stringList: StringList) {
        clear()
        addAll(stringList)
        nextId = stringList.nextId
    }

    // [id_1, content_1, id_2, content_2, ...]
    // [Int, String, Int, String, ...]
    override fun toJsonArray(): String {
        val cache = StringBuilder()
        cache.append('[')
        for (e in this) {
            cache.append(e.id)
            cache.append(',')
            cache.append('"').appendStringToJson(e.content).append('"')
            cache.append(',')
        }
        cache.deleteAt(cache.lastIndex)
        cache.append(']')
        return cache.toString()
    }
}

// 账户
class AccountList : MyArrayList<Account>() {

    override fun init(jsonArr: JSONArray) {
        val len = jsonArr.length()
        for (i in 0 until len) {
            val arr = jsonArr.getJSONArray(i)
            val id = arr.getInt(0)
            val name = arr.getString(1)
            val availability = arr.getBoolean(2)
            val assetsList = LinkedList<Assets>()
            var unusedAssetsList: LinkedList<UnusedAssets>? = null
            val arrLen = arr.length()
            for (j in 3 until arrLen step 4) {
                val idStr = arr.get(j).toString()
                if (idStr == "#") {
                    unusedAssetsList = LinkedList()
                    for (k in (j + 1) until arrLen step 3) {
                        unusedAssetsList.add(
                            UnusedAssets(
                                StringWithId(arr.getInt(k), arr.getString(k + 1)),
                                arr.getString(k + 2)
                            )
                        )
                    }
                    break
                } else {
                    assetsList.add(
                        Assets(
                            StringWithId(idStr.toInt(), arr.getString(j + 1)),
                            arr.getString(j + 2), arr.getString(j + 3)
                        )
                    )
                }
            }
            if (nextId <= id) {
                nextId = id + 1
            }
            val account = if (unusedAssetsList == null) {
                Account(id, name, availability, assetsList.toArray(emptyArray<Assets>()))
            } else {
                Account(id, name, availability, assetsList.toArray(emptyArray<Assets>()),
                    unusedAssetsList.toArray(emptyArray<UnusedAssets>()))
            }
            add(account)
            if (availability) {
                DataSource.availableAccount.add(account)
            }
        }
    }

    /**
     * 添加账户.
     *
     * !! 仅允许[DataSource.accountList]调用.
     */
    fun add(name: String, assetsList: Array<Assets>) {
        add(Account(nextId++, name, true, assetsList))
    }

    /**
     * 添加账户.
     */
    fun addAccount(account: Account) {
        if (nextId <= account.id) {
            nextId = account.id + 1
        }
        add(account)
    }

    /**
     * 替换账户列表的内容.
     *
     * !! 仅允许[DataSource.accountList]调用.
     */
    fun replace(accountList: AccountList) {
        clear()
        for (account in accountList) {
            add(account.copy())
        }
        nextId = accountList.nextId
    }

    fun copy(): AccountList {
        val copy = AccountList()
        for (account in this) {
            copy.add(account.copy())
        }
        copy.nextId = this.nextId
        return copy
    }

    /**
     * 使用[nextId]生成一个空账户.
     *
     * !! 仅允许[DataSource.accountList]调用.
     */
    fun generateEmptyAccount() = Account(nextId, "", true, emptyArray())

    // [[id, name, availability, typeId_1, typeContent_1, initValue_1, nowValue_1, ...,
    // #, unusedTypeId_1, unusedTypeContent_1, unusedValue_1, ...], [...], ...]
    // [[Int, String, Boolean, Int, String, String, String, ...,
    // String, Int, String, String, ...], [...], ...]
    override fun toJsonArray(): String {
        val cache = StringBuilder()
        cache.append('[')
        for (account in this) {
            cache.append('[')
            cache.append(account.id)
            cache.append(',')
            cache.append('"').appendStringToJson(account.name).append('"')
            cache.append(',')
            cache.append(account.availability)
            cache.append(',')
            for (assets in account.assetsList) {
                cache.append(assets.type.id)
                cache.append(',')
                cache.append('"').appendStringToJson(assets.type.content).append('"')
                cache.append(',')
                cache.append('"').append(assets.initValue).append('"')
                cache.append(',')
                cache.append('"').append(assets.nowValue).append('"')
                cache.append(',')
            }
            if (account.unusedAssetsList != null) {
                cache.append('#')
                cache.append(',')
                for (unusedAssets in account.unusedAssetsList!!) {
                    cache.append(unusedAssets.type.id)
                    cache.append(',')
                    cache.append('"').appendStringToJson(unusedAssets.type.content).append('"')
                    cache.append(',')
                    cache.append('"').append(unusedAssets.value).append('"')
                    cache.append(',')
                }
            }
            cache.deleteAt(cache.lastIndex)
            cache.append("],")
        }
        cache.deleteAt(cache.lastIndex)
        cache.append(']')
        return cache.toString()
    }
}

/**
 * 账户.
 *
 * 参数[name]为账号名称；参数[availability]为账户可用性；
 * 参数[assetsList]为该账号下现存资产；
 * 参数[unusedAssetsList]为该账号下被移除的资产.
 *
 * 所有资产（[assetsList]与[unusedAssetsList]）中各资产项type需不同。
 */
class Account(val id: Int, val name: String, val availability: Boolean,
              theAssetsList: Array<Assets>, theUnusedAssetsList: Array<UnusedAssets>? = null) {
    var assetsList = theAssetsList
        private set
    var unusedAssetsList = theUnusedAssetsList
        private set

    fun setName(name: String) = Account(id, name, availability, assetsList, unusedAssetsList)

    fun setAvailability(availability: Boolean) = Account(id, name, availability, assetsList, unusedAssetsList)

    /**
     * 从[assetsList]中移除[index]处的资产.
     *
     * 若被移除资产的[Assets.nowValue]与[Assets.initValue]不等，将记录到[unusedAssetsList]中.
     */
    fun deleteAssets(index: Int) {
        val assets = assetsList[index]
        val initValue = BigDecimal(assets.initValue)
        val nowValue = BigDecimal(assets.nowValue)
        // 从现存资产中移除
        assetsList = assetsList.remove(index)
        // 若被移除资产现值与初值相等，则不必记录
        if (initValue == nowValue) {
            return
        }
        // 记录移除的资产
        val unusedAssets = UnusedAssets(assets.type, String.format(Assets.format, nowValue.minus(initValue)))
        unusedAssetsList = if (unusedAssetsList == null) {
            arrayOf(unusedAssets)
        } else {
            unusedAssetsList!!.add(unusedAssets)
        }
    }

    /**
     * 根据[type]查找资产是否存在于[assetsList]中.
     */
    fun existsAssets(type: StringWithId): Boolean {
        for (assets in assetsList) {
            if (assets.type.id == type.id) {
                return true
            }
        }
        return false
    }

    /**
     * 根据[type]查找或添加资产.
     *
     * · 若查找的资产位于[assetsList]，则直接返回该资产.
     *
     * · 若查找的资产位于[unusedAssetsList]，将会将该资产以初值0加入[assetsList]后返回.
     *
     * · 若查找的资产不存在，将会新建该资产加入[assetsList]后返回.
     */
    fun findOrAddAssets(type: StringWithId): Assets {
        // 在现存资产中查找
        for (assets in assetsList) {
            if (assets.type.id == type.id) {
                return assets
            }
        }
        val zero = String.format(Assets.format, BigDecimal.ZERO)
        // 在被移除资产中查找
        if (unusedAssetsList != null) {
            for (i in unusedAssetsList!!.indices) {
                val unusedAssets = unusedAssetsList!![i]
                if (unusedAssets.type.id == type.id) {
                    // 以初值0创建该资产
                    val assets = Assets(unusedAssets.type, zero, unusedAssets.value)
                    // 从记录中移除
                    unusedAssetsList = unusedAssetsList!!.remove(i)
                    if (unusedAssetsList!!.isEmpty()) {
                        unusedAssetsList = null
                    }
                    // 添加到现存资产
                    assetsList = assetsList.add(assets)
                    return assets
                }
            }
        }
        // 新建该资产
        val assets = Assets(type, zero)
        // 添加到现存资产
        assetsList = assetsList.add(assets)
        return assets
    }

    fun copy(): Account {
        val newAssets = Array(assetsList.size) { assetsList[it].copy() }
        val newAccount = Account(id, name, availability, newAssets)
        if (this.unusedAssetsList != null) {
            newAccount.unusedAssetsList = Array(this.unusedAssetsList!!.size) { this.unusedAssetsList!![it] }
        }
        return newAccount
    }
}

/**
 * 资产.
 *
 * 参数[type]为资产类型，参数[initValue]为该资产初值，参数[nowValue]为资产现值.
 *
 * 参数[initValue]、[nowValue]须是"%.2f"格式化后的值.
 */
class Assets(val type: StringWithId, val initValue: String, theNowValue: String = initValue) {
    var nowValue = theNowValue
        private set

    companion object {
        const val format = "%.2f"
    }

    fun copy() = Assets(type, initValue, nowValue)

    fun setInitValue(initValue: String): Assets {
        val diff = BigDecimal(initValue).minus(BigDecimal(this.initValue))
        val nowValue = BigDecimal(this.nowValue).add(diff)
        return Assets(type, initValue, String.format(format, nowValue))
    }

    operator fun plusAssign(value: String) {
        nowValue = String.format(format, BigDecimal(nowValue).add(BigDecimal(value)))
    }

    operator fun minusAssign(value: String) {
        nowValue = String.format(format, BigDecimal(nowValue).minus(BigDecimal(value)))
    }
}

/**
 * 记录被移除资产的使用情况.
 *
 * 参数[type]是被移除的资产类型.
 *
 * 参数[value]是资产移除时[Assets.nowValue]与[Assets.initValue]的差.
 */
class UnusedAssets(val type: StringWithId, val value: String)

// 基金
class FundList : MyArrayList<Fund>() {

    override fun init(jsonArr: JSONArray) {
        val len = jsonArr.length()
        for (i in 0 until len step 3) {
            val id = jsonArr.getInt(i)
            add(Fund(id, jsonArr.getString(i + 1), jsonArr.getString(i + 2)))
            if (nextId <= id) {
                nextId = id + 1
            }
        }
    }

    fun add(name: String, code: String) {
        add(Fund(nextId++, name, code))
    }

    // [id_1, name_1, code_1, id_2, name_2, code_2, ...]
    // [Int, String, String, Int, String, String, ...]
    override fun toJsonArray(): String {
        val cache = StringBuilder()
        cache.append('[')
        for (fund in this) {
            cache.append(fund.id)
            cache.append(',')
            cache.append('"').appendStringToJson(fund.name).append('"')
            cache.append(',')
            cache.append('"').append(fund.code).append('"')
            cache.append(',')
        }
        cache.deleteAt(cache.lastIndex)
        cache.append(']')
        return cache.toString()
    }
}

/**
 * 基金.
 *
 * 参数[name]为基金名称，参数[code]为基金代码.
 */
class Fund(val id: Int, val name: String, val code: String)

// 科目
class SubjectArray {
    private var nextId = 1
    private lateinit var subject : Array<SubjectLvOne>

    fun init(source: Array<String>) {
        val lvOneList = LinkedList<SubjectLvOne>()
        val lvTwoList = LinkedList<SubjectLvTwo>()
        var lvOne: StringWithId? = null
        for (content in source) {
            if (content.startsWith('#')) {
                if (lvOne != null) {
                    lvOneList.add(SubjectLvOne(lvOne, if (lvTwoList.isEmpty()) null
                    else lvTwoList.toArray(emptyArray<SubjectLvTwo>())))
                    lvTwoList.clear()
                }
                lvOne = StringWithId(nextId++, content.substring(1))
            } else if (lvOne != null) {
                lvTwoList.add(SubjectLvTwo(StringWithId(nextId++, content)))
            }
        }
        if (lvOne != null) {
            lvOneList.add(SubjectLvOne(lvOne, if (lvTwoList.isEmpty()) null
            else lvTwoList.toArray(emptyArray<SubjectLvTwo>())))
        }
        subject = lvOneList.toArray(emptyArray<SubjectLvOne>())
    }

    fun init(jsonArr: JSONArray) {
        val lvOneList = LinkedList<SubjectLvOne>()
        val lvTwoList = LinkedList<SubjectLvTwo>()
        var lvOne: StringWithId? = null
        val len = jsonArr.length()
        for (i in 0 until len step 2) {
            val id = jsonArr.getInt(i)
            val content = jsonArr.getString(i + 1)
            if (content.startsWith('#')) {
                if (lvOne != null) {
                    lvOneList.add(SubjectLvOne(lvOne, if (lvTwoList.isEmpty()) null
                    else lvTwoList.toArray(emptyArray<SubjectLvTwo>())))
                    lvTwoList.clear()
                }
                lvOne = StringWithId(id, content.substring(1))
                if (nextId <= id) {
                    nextId = id + 1
                }
            } else if (lvOne != null) {
                lvTwoList.add(SubjectLvTwo(StringWithId(id, content)))
                if (nextId <= id) {
                    nextId = id + 1
                }
            }
        }
        if (lvOne != null) {
            lvOneList.add(SubjectLvOne(lvOne, if (lvTwoList.isEmpty()) null
            else lvTwoList.toArray(emptyArray<SubjectLvTwo>())))
        }
        subject = lvOneList.toArray(emptyArray<SubjectLvOne>())
    }

    fun clear() {
        nextId = 1
        subject = emptyArray()
    }

    fun toArray() = Array(subject.size) copy@{ i ->
        val lvOne = subject[i]
        val children = lvOne.subordinateData
        if (children == null) {
            return@copy SubjectLvOne(lvOne.data!!, null)
        } else {
            return@copy SubjectLvOne(lvOne.data!!, Array(children.size) { j ->
                SubjectLvTwo(children[j].data as StringWithId)
            })
        }
    }

    /**
     * 添加科目.
     *
     * 若[lvOneId]为空，则表示将[content]添加为一级科目；
     * 否则，添加为[lvOneId]所属一级科目的二级科目，
     * 若未找到所属一级科目则将[content]添加为一级科目.
     */
    fun addSubject(content: String, lvOneId: Int? = null) {
        if (lvOneId != null) { // 添加到二级科目
            for (i in subject.indices) {
                val lvOne = subject[i]
                if (lvOne.data!!.id == lvOneId) {
                    val children = lvOne.subordinateData
                    if (children == null) {
                        subject[i] = SubjectLvOne(lvOne.data, arrayOf(
                            SubjectLvTwo(StringWithId(nextId++, content))))
                    } else {
                        subject[i] = SubjectLvOne(lvOne.data, Array(children.size + 1) {
                            if (it < children.size) {
                                children[it]
                            } else {
                                SubjectLvTwo(StringWithId(nextId++, content))
                            }
                        })
                    }
                    return
                }
            }
            addSubject(content)
        } else {
            addSubject(content)
        }
    }

    // 添加到一级科目
    private fun addSubject(content: String) {
        subject = Array(subject.size + 1) {
            if (it < subject.size) {
                subject[it]
            } else {
                SubjectLvOne(StringWithId(nextId++, content))
            }
        }
    }

    /**
     * 删除科目.
     *
     * 删除[id]对应的科目.
     */
    fun delSubject(id: Int) {
        for (i in subject.indices) {
            val lvOne = subject[i]
            if (lvOne.data!!.id == id) {
                subject = Array(subject.size - 1) {
                    subject[if (it < i) it else it + 1]
                }
                return
            }
            val children = lvOne.subordinateData ?: continue
            for (j in children.indices) {
                val lvTwo = children[j]
                if (lvTwo is SubjectLvTwo && lvTwo.data!!.id == id) {
                    if (children.size <= 1) {
                        subject[i] = SubjectLvOne(lvOne.data)
                    } else {
                        subject[i] = SubjectLvOne(lvOne.data, Array(children.size - 1) {
                            children[if (it < j) it else it + 1]
                        })
                    }
                    return
                }
            }
        }
    }

    /**
     * 查找科目.
     *
     * 查找[id]对应的科目，若未查找到则返回null.
     */
    fun findSubject(id: Int): String? {
        for (lvOne in subject) {
            val prefix = lvOne.data!!.content
            if (lvOne.data.id == id) {
                return prefix
            }
            val children = lvOne.subordinateData ?: continue
            for (lvTwo in children) {
                if (lvTwo is SubjectLvTwo && lvTwo.data!!.id == id) {
                    return "$prefix/${lvTwo.data.content}"
                }
            }
        }
        return null
    }

    /**
     * 修改科目.
     *
     * 将[id]对应的科目名称修改为[newName].
     */
    fun alterSubject(id: Int, newName: String) {
        for (i in subject.indices) {
            val lvOne = subject[i]
            val children = lvOne.subordinateData
            if (lvOne.data!!.id == id) {
                subject[i] = SubjectLvOne(StringWithId(id, newName), children)
                return
            }
            if (children == null) {
                continue
            }
            for (j in children.indices) {
                val lvTwo = children[j]
                if (lvTwo is SubjectLvTwo && lvTwo.data!!.id == id) {
                    children[j] = SubjectLvTwo(StringWithId(id, newName))
                    return
                }
            }
        }
    }

    // [id, #一级科目_1, id, 二级科目_1_1, ..., id, #一级科目_2, id, 二级科目_2_1, ...]
    // [Int, String, Int, String, ..., Int, String, Int, String, ...]
    fun toJsonArray(): String {
        val cache = StringBuilder()
        cache.append('[')
        for (one in subject) {
            cache.append(one.data!!.id)
            cache.append(',')
            cache.append("\"#").appendStringToJson(one.data.content).append('"')
            cache.append(',')
            val lvTwo = one.subordinateData
            if (lvTwo != null) {
                for (two in lvTwo) {
                    if (two is SubjectLvTwo) {
                        cache.append(two.data!!.id)
                        cache.append(',')
                        cache.append('"').appendStringToJson(two.data.content).append('"')
                        cache.append(',')
                    }
                }
            }
        }
        cache.deleteAt(cache.lastIndex)
        cache.append(']')
        return cache.toString()
    }

    override fun toString() = toJsonArray()
}

class SubjectLvOne(data: StringWithId, children: Array<Data<*, *>>? = null) :
    Data<StringWithId, SubjectLvOne.ViewHolder>(data, children) {

    override fun getResId() = R.layout.item_dialog_subject_level_one

    override fun getLevel() = 0

    override fun newViewHolder(view: View): ViewHolder {
        return ViewHolder().apply {
            this.content = view.findViewById(R.id.content)
        }
    }

    override fun loadingDataOnView(context: Context, viewHolder: Any, position: Int) {
        if (viewHolder is ViewHolder) {
            viewHolder.content.text = data!!.content
        }
    }

    class ViewHolder {
        lateinit var content: DrawableTextView
    }
}

class SubjectLvTwo(data: StringWithId) : Data<StringWithId, SubjectLvTwo.ViewHolder>(data) {

    override fun getResId() = R.layout.item_dialog_subject_level_two

    override fun getLevel() = 1

    override fun newViewHolder(view: View): ViewHolder {
        return ViewHolder().apply {
            this.content = view.findViewById(R.id.content)
        }
    }

    override fun loadingDataOnView(context: Context, viewHolder: Any, position: Int) {
        if (viewHolder is ViewHolder) {
            viewHolder.content.text = data!!.content
        }
    }

    class ViewHolder {
        lateinit var content: TextView
    }
}
