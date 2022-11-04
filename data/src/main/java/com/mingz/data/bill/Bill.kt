package com.mingz.data.bill

import android.os.Parcel
import java.math.BigDecimal

/**
 * 表示账单的父类.
 *
 * 子类需要指定公有静态整型常量“typeId”用以指定账单类型id，各子类该值应不同.
 */
abstract class Bill(
    /**
     * 账单id.
     *
     * 从数据库中生成或读取.
     */
    val dataId: Int,

    /**
     * 账单产生时间.
     *
     * 格式为“yyyy-MM-dd HH:mm”.
     */
    val time: String
) {
    companion object {
        /**
         * 精确到百分位的0值.
         */
        const val ZERO_2 = "0.00"

        /**
         * 精确到万分位的0值.
         */
        const val ZERO_4 = "0.0000"

        /**
         * 精确到百分位的格式串.
         */
        private const val FORMAT_2 = "%.2f"

        /**
         * 精确度万分位的格式串.
         */
        private const val FORMAT_4 = "%.4f"

        /**
         * 日期时间的格式.
         */
        const val FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm"

        /**
         * 日期的格式.
         */
        const val FORMAT_DATE = "yyyy-MM-dd"

        /**
         * 当id为该值时，意味着该id或id代表的数据应记为null.
         */
        const val NULL_ID = -1

        /**
         * 使用[FORMAT_2]格式化[number].
         */
        fun formatBigDecimal2(number: BigDecimal) = String.format(FORMAT_2, number)

        /**
         * 使用[FORMAT_4]格式化[number].
         */
        fun formatBigDecimal4(number: BigDecimal) = String.format(FORMAT_4, number)

        /**
         * 如果id是可空的，使用该方法替代[Parcel.writeInt]写入[id].
         */
        internal fun writeIdIfCanBeNull(parcel: Parcel, id: Int?) {
            parcel.writeInt(id ?: NULL_ID)
        }

        /**
         * 如果要读取的id是可空的，使用该方法替代[Parcel.readInt]读取id.
         */
        internal fun readIdIfCanBeNull(parcel: Parcel): Int? {
            val id = parcel.readInt()
            return if (id == NULL_ID) null else id
        }
    }

    override fun toString(): String {
        val typeId: Any = when (this) {
            is Expenditure -> Expenditure.typeId
            is Income -> Income.typeId
            is Transfer -> Transfer.typeId
            is FundPurchase -> FundPurchase.typeId
            is FundSales -> FundSales.typeId
            is FundDividend -> FundDividend.typeId
            is FundTransfer -> FundTransfer.typeId
            else -> "Null"
        }
        return "Bill($typeId - ${if (dataId == NULL_ID) "Null" else dataId} $time)"
    }
}