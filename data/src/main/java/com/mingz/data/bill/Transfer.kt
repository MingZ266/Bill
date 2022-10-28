package com.mingz.data.bill

import android.os.Parcel
import android.os.Parcelable

/**
 * 转账.
 */
class Transfer(
    /**
     * 转出账户id.
     */
    val outAccount: Int,

    /**
     * 转入账户id.
     */
    val inAccount: Int,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 转出金额.
     *
     * 精确到百分位.
     */
    val outPrice: String,

    /**
     * 手续费.
     *
     * 精确到百分位.
     */
    val charges: String,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 转账时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = NULL_ID
) : Bill(id, time), Parcelable {
    companion object {
        const val typeId = 3

        val CREATOR = object : Parcelable.Creator<Transfer?> {
            override fun createFromParcel(parcel: Parcel) = try {
                Transfer(
                    parcel.readInt(), // outAccount
                    parcel.readInt(), // inAccount
                    parcel.readInt(), // type
                    parcel.readString()!!, // outPrice
                    parcel.readString()!!, // charges
                    parcel.readString(), // remark
                    parcel.readString()!!, // time
                    parcel.readInt() // dataId
                )
            } catch (e: NullPointerException) { null }

            override fun newArray(size: Int) = arrayOfNulls<Transfer>(size)
        }
    }

    @Suppress("DuplicatedCode")
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(outAccount)
        parcel.writeInt(inAccount)
        parcel.writeInt(type)
        parcel.writeString(outPrice)
        parcel.writeString(charges)
        parcel.writeString(remark)
        parcel.writeString(time)
        parcel.writeInt(dataId)
    }

    override fun describeContents() = 0
}