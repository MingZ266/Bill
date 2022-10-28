package com.mingz.data.bill

import android.os.Parcel
import android.os.Parcelable

/**
 * 支出.
 */
class Expenditure(
    /**
     * 支出科目id.
     */
    val subject: Int,

    /**
     * 付款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实际支出金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 优惠.
     *
     * 精确到百分位.
     */
    val discount: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 支出时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = NULL_ID
) : Bill(id, time), Parcelable {
    companion object {
        const val typeId = 1

        val CREATOR = object : Parcelable.Creator<Expenditure?> {
            override fun createFromParcel(parcel: Parcel) = try {
                Expenditure(
                    parcel.readInt(), // subject
                    parcel.readInt(), // account
                    parcel.readInt(), // type
                    parcel.readString()!!, // price
                    parcel.readString()!!, // discount
                    parcel.readString(), // remark
                    parcel.readString()!!, // time
                    parcel.readInt() // dataId
                )
            } catch (e: NullPointerException) { null }

            override fun newArray(size: Int) = arrayOfNulls<Expenditure>(size)
        }
    }

    @Suppress("DuplicatedCode")
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(subject)
        parcel.writeInt(account)
        parcel.writeInt(type)
        parcel.writeString(price)
        parcel.writeString(discount)
        parcel.writeString(remark)
        parcel.writeString(time)
        parcel.writeInt(dataId)
    }

    override fun describeContents() = 0
}