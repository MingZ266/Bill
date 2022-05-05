package com.mingz.billing.entities

abstract class Income : Billing() {

    companion object {
        const val typeId = 2
        const val typeDesc = "收入"

        @JvmStatic
        fun fromStringData(data: String): Income? {
            TODO("逆向解析")
        }
    }

    override val typeId = Income.typeId
    override val typeDesc = Income.typeDesc

    override fun toStringData(): String {
        TODO("Not yet implemented")
    }
}