package com.mingz.data.bill

import android.os.Parcel
import android.os.Parcelable

/**
 * 基金转换.
 */
class FundTransfer(
    /**
     * 转出基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val outFund: String,

    /**
     * 转出份额.
     *
     * 精确到百分位.
     */
    val outAmount: String,

    /**
     * 转出基金净值.
     *
     * 精确到万分位.
     */
    val outNetVal: String,

    /**
     * 总的手续费.
     *
     * 精确到百分位.
     */
    val charges: String,

    /**
     * 转入基金.
     *
     * 格式为“（基金编号）基金名称”.
     */
    val inFund: String,

    /**
     * 转入份额.
     *
     * 精确到百分位.
     */
    val inAmount: String,

    /**
     * 转入基金净值.
     *
     * 精确到万分位.
     */
    val inNetVal: String,

    /**
     * 动账时间.
     *
     * 格式为“yyyy-MM-dd HH:mm”，若实际尾差为0，则为空，否则非空.
     */
    val timeForAccount: String?,

    /**
     * 动账账户id.
     *
     * 当基金转换有尾差时，使用该账户多退少补，此时非空，否则为空.
     */
    val account: Int?,

    /**
     * 币种id.
     */
    val type: Int,

    /**
     * 实际动账金额.
     *
     * 即实际尾差；精确到百分位；正值表示退款，负值表示补款.
     * 若实际尾差为0，则为空，否则非空.
     */
    val price: String?,

    /**
     * 备注.
     */
    val remark: String?,

    /**
     * 转换时间.
     * @see Bill.time
     */
    time: String,

    /**
     * @see Bill.dataId
     */
    id: Int = NULL_ID
) : Bill(id, time), Parcelable {
    companion object {
        const val typeId = 7

        val CREATOR = object : Parcelable.Creator<FundTransfer?> {
            override fun createFromParcel(parcel: Parcel) = try {
                FundTransfer(
                    parcel.readString()!!, // outFund
                    parcel.readString()!!, // outAmount
                    parcel.readString()!!, // outNetVal
                    parcel.readString()!!, // charges
                    parcel.readString()!!, // inFund
                    parcel.readString()!!, // inAmount
                    parcel.readString()!!, // inNetVal
                    parcel.readString(), // timeForAccount
                    readIdIfCanBeNull(parcel), // account
                    parcel.readInt(), // type
                    parcel.readString(), // price
                    parcel.readString(), // remark
                    parcel.readString()!!, // time
                    parcel.readInt() // dataId
                )
            } catch (e: NullPointerException) { null }

            override fun newArray(size: Int) = arrayOfNulls<FundTransfer>(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(outFund)
        parcel.writeString(outAmount)
        parcel.writeString(outNetVal)
        parcel.writeString(charges)
        parcel.writeString(inFund)
        parcel.writeString(inAmount)
        parcel.writeString(inNetVal)
        parcel.writeString(timeForAccount)
        writeIdIfCanBeNull(parcel, account)
        parcel.writeInt(type)
        parcel.writeString(price)
        parcel.writeString(remark)
        parcel.writeString(time)
        parcel.writeInt(dataId)
    }

    override fun describeContents() = 0
}