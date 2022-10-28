package com.mingz.data.bill

import android.os.Parcel
import android.os.Parcelable

/**
 * 基金分红.
 */
class FundDividend(
    /**
     * 基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val fund: String,

    /**
     * 分红金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 是否再投资.
     *
     * 默认否.
     */
    val redo: Boolean,

    /**
     * 收款账户id.
     *
     * 若再投资，则为空，否则非空.
     */
    val account: Int?,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 基金净值.
     *
     * 精确到万分位；若再投资，则非空，否则为空.
     */
    val netVal: String?,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 分红时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = NULL_ID
) : Bill(id, time), Parcelable {
    companion object {
        const val typeId = 6

        val CREATOR = object : Parcelable.Creator<FundDividend?> {
            override fun createFromParcel(parcel: Parcel) = try {
                FundDividend(
                    parcel.readString()!!, // fund
                    parcel.readString()!!, // price
                    parcel.readBoolean(), // redo
                    readIdIfCanBeNull(parcel), // account
                    parcel.readInt(), // type
                    parcel.readString(), // netVal
                    parcel.readString(), // remark
                    parcel.readString()!!, // time
                    parcel.readInt() // dataId
                )
            } catch (e: NullPointerException) { null }

            override fun newArray(size: Int) = arrayOfNulls<FundDividend>(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fund)
        parcel.writeString(price)
        parcel.writeBoolean(redo)
        writeIdIfCanBeNull(parcel, account)
        parcel.writeInt(type)
        parcel.writeString(netVal)
        parcel.writeString(remark)
        parcel.writeString(time)
        parcel.writeInt(dataId)
    }

    override fun describeContents() = 0
}