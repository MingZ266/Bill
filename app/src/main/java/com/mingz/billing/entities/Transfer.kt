package com.mingz.billing.entities

abstract class Transfer : Billing() {

    companion object {
        const val typeId = 3
        const val typeDesc = "转账"

        @JvmStatic
        fun fromStringData(data: String): Transfer? {
            TODO("逆向解析")
        }
    }

    override val typeId = Transfer.typeId
    override val typeDesc = Transfer.typeDesc

    override fun toStringData(): String {
        TODO("Not yet implemented")
    }
}