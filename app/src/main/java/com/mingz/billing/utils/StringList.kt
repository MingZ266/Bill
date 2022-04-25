package com.mingz.billing.utils

class StringList : ArrayList<StringList.StringWithId>() {
    private var nextId = 1

    fun add(content: String) {
        add(StringWithId(nextId++, content))
    }

    fun floatToTop(index: Int) {
        val element = removeAt(index)
        add(0, element)
    }

    // 形如：["...","...","..."]
    override fun toString(): String {
        val cache = StringBuilder()
        cache.append('[')
        for (i in 0 until size) {
            cache.append('"').append(this[i].content).append('"')
            if (i != lastIndex) {
                cache.append(',')
            }
        }
        cache.append(']')
        return cache.toString()
    }

    class StringWithId(val id: Int, val content: String)
}