package com.mingz.data.bill

import android.os.Parcel
import android.os.Parcelable

/**
 * 基金买入.
 */
class FundPurchase(
    /**
     * 基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val fund: String,

    /**
     * 付款账户id.
     */
    val account: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实付金额.
     *
     * 精确到百分位.
     */
    val price: String,

    /**
     * 优惠金额.
     *
     * 精确到百分位.
     */
    val discount: String,

    /**
     * 确认日期.
     *
     * 格式为“yyyy-MM-dd”.
     */
    val confirmDate: String,

    /**
     * 确认净值.
     *
     * 精确到万分位.
     */
    val netVal: String,

    /**
     * 买入手续费.
     *
     * 精确到百分位.
     */
    val charges: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 付款购买时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = NULL_ID
) : Bill(id, time), Parcelable {
    companion object {
        const val typeId = 4

        val CREATOR = object : Parcelable.Creator<FundPurchase?> {
            override fun createFromParcel(parcel: Parcel) = try {
                FundPurchase(
                    parcel.readString()!!, // fund
                    parcel.readInt(), // account
                    parcel.readInt(), // type
                    parcel.readString()!!, // price
                    parcel.readString()!!, // discount
                    parcel.readString()!!, // confirmDate
                    parcel.readString()!!, // netVal
                    parcel.readString()!!, // charges
                    parcel.readString(), // remark
                    parcel.readString()!!, // time
                    parcel.readInt() // dataId
                )
            } catch (e: NullPointerException) { null }

            override fun newArray(size: Int) = arrayOfNulls<FundPurchase>(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fund)
        parcel.writeInt(account)
        parcel.writeInt(type)
        parcel.writeString(price)
        parcel.writeString(discount)
        parcel.writeString(confirmDate)
        parcel.writeString(netVal)
        parcel.writeString(charges)
        parcel.writeString(remark)
        parcel.writeString(time)
        parcel.writeInt(dataId)
    }

    override fun describeContents() = 0
}