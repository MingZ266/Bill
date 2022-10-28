package com.mingz.data.bill

import android.os.Parcel
import android.os.Parcelable

/**
 * 收入.
 */
class Income(
    /**
     * 收入科目id.
     */
    val subject: Int,

    /**
     * 收款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 收入时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = NULL_ID
) : Bill(id, time), Parcelable {
    companion object {
        const val typeId = 2

        val CREATOR = object : Parcelable.Creator<Income?> {
            override fun createFromParcel(parcel: Parcel) = try {
                Income(
                    parcel.readInt(), // subject
                    parcel.readInt(), // account
                    parcel.readInt(), // type
                    parcel.readString()!!, // price
                    parcel.readString(), // remark
                    parcel.readString()!!, // time
                    parcel.readInt() // dataId
                )
            } catch (e: NullPointerException) { null }

            override fun newArray(size: Int) = arrayOfNulls<Income>(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(subject)
        parcel.writeInt(account)
        parcel.writeInt(type)
        parcel.writeString(price)
        parcel.writeString(remark)
        parcel.writeString(time)
        parcel.writeInt(dataId)
    }

    override fun describeContents() = 0
}