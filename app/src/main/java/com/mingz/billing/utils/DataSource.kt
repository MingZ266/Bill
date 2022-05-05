package com.mingz.billing.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import com.mingz.billing.R
import com.mingz.billing.ui.DrawableTextView
import com.mingz.billing.ui.MultilevelListView.Data
import com.mingz.billing.utils.Tools.Companion.appendStringToJson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.reflect.KProperty

class DataSource private constructor() {

    companion object {
        private val myLog = MyLog(this)
        private val file by InitFile()

        /**
         * 所有账户.
         */
        @JvmField
        val accountList = StringList()

        /**
         * 所有货币类型.
         */
        @JvmField
        val typeList = StringList()

        /**
         * 所有基金.
         */
        @JvmField
        val fundList = StringList()

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
                    accountList.clear()
                    typeList.clear()
                    fundList.clear()
                    expenditureSubject.clear()
                    incomeSubject.clear()
                    initUseDefault(context)
                }
            }
        }

        private fun initUseDefault(context: Context) {
            // TODO: delete it
            if (myLog.debug) {
                accountList.add("账户A")
                accountList.add("账户B")
                accountList.add("账户C")
                accountList.add("账户D")
                accountList.add("账户E")
                accountList.add("账户F")
                accountList.add("账户G")
                accountList.add("账户H")
                fundList.add("基金A")
                fundList.add("基金B")
                fundList.add("基金C")
                fundList.add("基金D")
                fundList.add("基金E")
                fundList.add("基金F")
            }
            val res = context.resources
            typeList.init(res.getStringArray(R.array.defaultType))
            expenditureSubject.init(res.getStringArray(R.array.defaultExpenditure))
            incomeSubject.init(res.getStringArray(R.array.defaultIncome))
        }

        @JvmStatic
        fun save() {
            Tools.saveFile(file, Encryption.encryptIfNeed(toJson()))
        }

        private fun toJson(): String {
            val cache = StringBuilder()
            cache.append('{')
            cache.append("\"ac\":").append(accountList.toString())
            cache.append(',')
            cache.append("\"ty\":").append(typeList.toString())
            cache.append(',')
            cache.append("\"fu\":").append(fundList.toString())
            cache.append(',')
            cache.append("\"ex\":").append(expenditureSubject.toString())
            cache.append(',')
            cache.append("\"in\":").append(incomeSubject.toString())
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

class StringWithId(val id: Int, val content: String)

class StringList : ArrayList<StringWithId>() {
    private var nextId = 1

    fun init(source: Array<String>) {
        for (data in source) {
            add(data)
        }
    }

    fun init(jsonArr: JSONArray) {
        var id: Int? = null
        val len = jsonArr.length()
        for (i in 0 until len) {
            val str = jsonArr.getString(i)
            id = if (id == null) {
                str.toInt()
            } else {
                if (nextId <= id) {
                    nextId = id + 1
                }
                add(StringWithId(id, str))
                null
            }
        }
    }

    override fun clear() {
        super.clear()
        nextId = 1
    }

    fun add(content: String) {
        add(StringWithId(nextId++, content))
    }

    fun floatToTop(index: Int) {
        val element = removeAt(index)
        add(0, element)
    }

    // 形如：["id_1","content_1","id_2","content_2","id_2","content_2"]
    override fun toString(): String {
        val cache = StringBuilder()
        cache.append('[')
        for (e in this) {
            cache.append('"').append(e.id).append('"')
            cache.append(',')
            cache.append('"').appendStringToJson(e.content).append('"')
            cache.append(',')
        }
        cache.deleteAt(cache.lastIndex)
        cache.append(']')
        return cache.toString()
    }
}

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
        var id: Int? = null
        val lvOneList = LinkedList<SubjectLvOne>()
        val lvTwoList = LinkedList<SubjectLvTwo>()
        var lvOne: StringWithId? = null
        val len = jsonArr.length()
        for (i in 0 until len) {
            val str = jsonArr.getString(i)
            id = if (id == null) {
                str.toInt()
            } else {
                if (str.startsWith('#')) {
                    if (lvOne != null) {
                        lvOneList.add(SubjectLvOne(lvOne, if (lvTwoList.isEmpty()) null
                        else lvTwoList.toArray(emptyArray<SubjectLvTwo>())))
                        lvTwoList.clear()
                    }
                    lvOne = StringWithId(id, str.substring(1))
                    if (nextId <= id) {
                        nextId = id + 1
                    }
                } else if (lvOne != null) {
                    lvTwoList.add(SubjectLvTwo(StringWithId(id, str)))
                    if (nextId <= id) {
                        nextId = id + 1
                    }
                }
                null
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

    override fun toString(): String {
        val cache = StringBuilder()
        cache.append('[')
        for (one in subject) {
            cache.append('"').append(one.data!!.id).append('"')
            cache.append(',')
            cache.append("\"#").appendStringToJson(one.data.content).append('"')
            cache.append(',')
            val lvTwo = one.subordinateData
            if (lvTwo != null) {
                for (two in lvTwo) {
                    if (two is SubjectLvTwo) {
                        cache.append('"').append(two.data!!.id).append('"')
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
