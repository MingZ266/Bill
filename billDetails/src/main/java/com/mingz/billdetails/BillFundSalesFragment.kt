package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundSalesBinding
import com.mingz.data.*
import com.mingz.data.bill.Bill
import com.mingz.data.bill.FundSales
import com.mingz.share.MyLog
import com.mingz.share.showToast
import com.mingz.share.ui.InputAmount
import java.math.BigDecimal
import java.math.RoundingMode

class BillFundSalesFragment : BillFragmentImpl<FundSales>() {
    private lateinit var binding: FragmentBillFundSalesBinding
    private var account: Account? = null // 收款账户
    private lateinit var type: Type // 币种
    private lateinit var confirmVal: BigDecimal // 确认净值
    private lateinit var confirmAmount: BigDecimal // 确认份额
    private lateinit var price: BigDecimal // 实收金额
    private lateinit var charges: BigDecimal // 手续费 = 确认净值 * 确认份额 - 实收金额

    override fun createMyLog() = MyLog("基金卖出")

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            account = null
            type = typeSet[0]
            confirmVal = BigDecimal.ZERO
            confirmAmount = BigDecimal.ZERO
            price = BigDecimal.ZERO
            charges = BigDecimal.ZERO
        } else {
            account = findAccount(bill.account)
            type = findType(bill.type) ?: typeSet[0]
            confirmVal = BigDecimal(bill.netVal)
            confirmAmount = BigDecimal(bill.amount)
            price = BigDecimal(bill.price)
            charges = confirmVal.multiply(confirmAmount).setScale(2, RoundingMode.HALF_UP)
                .subtract(price).setScale(2, RoundingMode.HALF_UP)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        val bill = bill
        if (bill == null) {
            binding.salesTime.updateToNowTime()
            binding.inAccountTime.setDateTime(binding.salesTime)
            binding.confirmVal.text = Bill.ZERO_4
            binding.confirmAmount.text = Bill.ZERO_2
            binding.charges.text = Bill.ZERO_2
            binding.price.amount = Bill.ZERO_2
        } else {
            binding.salesTime.setDateTime(bill.salesTime, Bill.FORMAT_DATE_TIME)
            binding.inAccountTime.setDateTime(bill.time, Bill.FORMAT_DATE_TIME)
            binding.confirmVal.text = Bill.formatBigDecimal4(confirmVal)
            binding.confirmAmount.text = Bill.formatBigDecimal2(confirmAmount)
            binding.charges.text = Bill.formatBigDecimal2(charges)
            binding.price.amount = Bill.formatBigDecimal2(price)
        }
        binding.account.content = account?.name ?: ""
        binding.price.setUnits(type.name)
        initEnable()
        myListener()
    }

    private fun initEnable() {
        setEnabled(mode != MODE_CHECK, binding.fund, binding.salesTime, binding.inAccountTime,
            binding.confirmVal, binding.confirmAmount, binding.account, binding.price, binding.remark)
    }

    private fun myListener() {
        binding.fund.setOnClickListener {
            context?.showToast("基金")
        }

        binding.account.setOnClickListener {
            context?.showToast("选择收款账户")
        }

        // 确认净值和确认份额点击监听
        val mClickListener = View.OnClickListener {
            val isNetVal = it.id == R.id.confirmVal
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                if (isNetVal) {
                    title.text = getString(R.string.confirmVal)
                    input.initParams(confirmVal, 4)
                } else {
                    title.text = getString(R.string.confirmAmount)
                    input.initParams(confirmAmount)
                }
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        if (isNetVal) {
                            confirmVal = BigDecimal(value)
                            binding.confirmVal.text = value
                        } else {
                            confirmAmount = BigDecimal(value)
                            binding.confirmAmount.text = value
                        }
                        val confirmPrice = confirmVal.multiply(confirmAmount) // 确认金额
                        // 手续费
                        if (charges > confirmPrice) { // 手续费超出确认金额，手续费置为0
                            charges = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                            binding.charges.text = Bill.ZERO_2
                        }
                        // 实收金额
                        price = confirmPrice.subtract(charges)
                        binding.price.amount = Bill.formatBigDecimal2(price)
                    }
                })
            }
        }
        binding.confirmVal.setOnClickListener(mClickListener) // 确认净值
        binding.confirmAmount.setOnClickListener(mClickListener) // 确认份额

        binding.price.setOnClickListener { // 实收金额
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                title.text = getString(R.string.price)
                input.initParams(price)
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        val temp = BigDecimal(value)
                        // 确认金额 = 确认净值 * 确认份额
                        val confirmPrice = confirmVal.multiply(confirmAmount).setScale(2, RoundingMode.HALF_UP)
                        if (temp > confirmPrice) { // 实收金额超出确认金额
                            context?.showToast("实收金额不应超出${Bill.formatBigDecimal2(confirmPrice)}")
                            return
                        }
                        inputAmountDialog.dismiss()
                        price = temp
                        binding.price.amount = value
                        // 手续费 = 确认金额 - 实收金额
                        charges = confirmPrice.subtract(price).setScale(2, RoundingMode.HALF_UP)
                        binding.charges.text = Bill.formatBigDecimal2(charges)
                    }
                })
            }
        }
    }

    override fun updateBill() {
        bill = FundSales(
            binding.fund.content,
            binding.salesTime.getDateTime(Bill.FORMAT_DATE_TIME),
            binding.confirmVal.text.toString(),
            binding.confirmAmount.text.toString(),
            account?.id ?: Bill.NULL_ID,
            type.id,
            binding.price.amount,
            binding.remark.getContentOrNull(),
            binding.inAccountTime.getDateTime(Bill.FORMAT_DATE_TIME),
            bill?.dataId ?: Bill.NULL_ID
        )
    }

    override fun onDispatchActionDown(x: Float, y: Float) {
        if (!isHidden) {
            binding.remark.clearFocusWhenParentDispatchDown(x, y)
        }
    }

    companion object {
        const val TAG = FundSales.typeId.toString()

        /**
         * 必须检查[typeSet]不为空集合，可以检查[accountSet]不为空集合.
         */
        @JvmStatic
        fun newInstance(bill: FundSales? = null, mode: Boolean? = MODE_ADD) =
            BillFundSalesFragment().apply { initArguments(bill, mode) }
    }
}