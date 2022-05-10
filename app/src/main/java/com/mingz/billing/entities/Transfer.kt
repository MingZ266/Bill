package com.mingz.billing.entities

import org.json.JSONArray

abstract class Transfer : Billing() {

    companion object {
        const val typeId = 3
        const val typeDesc = "转账"

        @JvmStatic
        fun fromStringData(jsonArray: JSONArray): Transfer? {
            TODO("逆向解析")
        }
    }

    override val typeId = Transfer.typeId
    override val typeDesc = Transfer.typeDesc

    override fun toJsonArray(): String {
        TODO("Not yet implemented")
    }
}