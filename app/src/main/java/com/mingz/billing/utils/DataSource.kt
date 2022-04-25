package com.mingz.billing.utils

import android.content.Context
import android.widget.CheckedTextView
import com.mingz.billing.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.jvm.Throws

class DataSource private constructor() {
    private val myLog = MyLog(this)

    /**
     * 所有账户.
     */
    val accountList = StringList()

    /**
     * 所有货币类型.
     */
    val typeList = StringList()

    /**
     * 所有基金.
     */
    val fundList = StringList()

    init {
        if (myLog.debug) {
            fundList.add("基金A")
            fundList.add("基金B")
            fundList.add("基金C")
            fundList.add("基金D")
            fundList.add("基金E")
            fundList.add("基金F")
        }
    }

    companion object {
        @JvmStatic
        val INSTANCE = DataSource()
    }

    @Throws(JSONException::class)
    fun init(json: String) {
        val jsonObj = JSONObject(json)
        readToList(jsonObj.getJSONArray("account"), accountList)
        readToList(jsonObj.getJSONArray("type"), typeList)
        readToList(jsonObj.getJSONArray("fund"), fundList)
    }

    fun init(context: Context) {
        val defaultAccount = context.resources.getStringArray(R.array.defaultAccount)
        for (data in defaultAccount) {
            accountList.add(data)
        }
        val defaultType = context.resources.getStringArray(R.array.defaultType)
        for (data in defaultType) {
            typeList.add(data)
        }
    }

    fun toJson(): String {
        val cache = StringBuilder()
        cache.append("{\"account\":").append(accountList.toString())
        cache.append(",\"type\":").append(typeList.toString())
        cache.append(",\"fund\":").append(fundList.toString())
        return cache.toString()
    }

    private fun readToList(jsonArr: JSONArray, list: StringList) {
        for (i in 0 until jsonArr.length()) {
            list.add(jsonArr.getString(i))
        }
    }

    class SubjectViewHolder {
        lateinit var content: CheckedTextView
    }
}