package com.mingz.data.bill

import android.os.Parcel
import android.os.Parcelable

/**
 * 基金卖出.
 */
class FundSales(
    /**
     * 基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val fund: String,

    /**
     * 卖出时间.
     *
     * 格式为“yyyy-MM-dd HH:mm”.
     */
    val salesTime: String,

    /**
     * 确认净值.
     *
     * 精确到万分位.
     */
    val netVal: String,

    /**
     * 确认份额.
     *
     * 精确到百分位.
     */
    val amount: String,

    /**
     * 收款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实收金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 到账时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = NULL_ID
) : Bill(id, time), Parcelable {
    companion object {
        const val typeId = 5

        val CREATOR = object : Parcelable.Creator<FundSales?> {
            override fun createFromParcel(parcel: Parcel) = try {
                FundSales(
                    parcel.readString()!!, // fund
                    parcel.readString()!!, // salesTime
                    parcel.readString()!!, // netVal
                    parcel.readString()!!, // amount
                    parcel.readInt(), // account
                    parcel.readInt(), // type
                    parcel.readString()!!, // price
                    parcel.readString(), // remark
                    parcel.readString()!!, // time
                    parcel.readInt() // dataId
                )
            } catch (e: NullPointerException) { null }

            override fun newArray(size: Int) = arrayOfNulls<FundSales>(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fund)
        parcel.writeString(salesTime)
        parcel.writeString(netVal)
        parcel.writeString(amount)
        parcel.writeInt(account)
        parcel.writeInt(type)
        parcel.writeString(price)
        parcel.writeString(remark)
        parcel.writeString(time)
        parcel.writeInt(dataId)
    }

    override fun describeContents() = 0
}