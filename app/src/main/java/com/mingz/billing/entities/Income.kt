package com.mingz.billing.entities

import org.json.JSONArray

abstract class Income : Billing() {

    companion object {
        const val typeId = 2
        const val typeDesc = "收入"

        @JvmStatic
        fun fromStringData(jsonArray: JSONArray): Income? {
            TODO("逆向解析")
        }
    }

    override val typeId = Income.typeId
    override val typeDesc = Income.typeDesc

    override fun toJsonArray(): String {
        TODO("Not yet implemented")
    }
}