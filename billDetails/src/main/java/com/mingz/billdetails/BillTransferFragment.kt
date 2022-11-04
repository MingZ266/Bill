package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillTransferBinding
import com.mingz.data.*
import com.mingz.data.bill.Bill
import com.mingz.data.bill.Transfer
import com.mingz.share.MyLog
import com.mingz.share.showToast
import com.mingz.share.ui.InputAmount
import java.math.BigDecimal

class BillTransferFragment : BillFragmentImpl<Transfer>() {
    private lateinit var binding: FragmentBillTransferBinding
    private var outAccount: Account? = null // 转出账户
    private var inAccount: Account? = null // 转入账户
    private lateinit var type: Type // 币种
    private lateinit var outPrice: BigDecimal // 转出金额
    private lateinit var charges: BigDecimal // 手续费
    private lateinit var inPrice: String // 转入金额 = 转出金额 - 手续费

    override fun createMyLog() = MyLog("转账")

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            outAccount = null
            inAccount = null
            type = typeSet[0]
            outPrice = BigDecimal.ZERO
            charges = BigDecimal.ZERO
            inPrice = Bill.ZERO_2
        } else {
            outAccount = findAccount(bill.outAccount)
            inAccount = findAccount(bill.inAccount)
            type = findType(bill.type) ?: typeSet[0]
            outPrice = BigDecimal(bill.outPrice)
            charges = BigDecimal(bill.charges)
            inPrice = Bill.formatBigDecimal2(outPrice.subtract(charges))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        if (bill == null) {
            binding.outPrice.amount = Bill.ZERO_2
            binding.charges.amount = Bill.ZERO_2
            binding.time.updateToNowTime()
        } else {
            binding.outPrice.amount = Bill.formatBigDecimal2(outPrice)
            binding.charges.amount = Bill.formatBigDecimal2(charges)
            binding.time.setDateTime(bill!!.time, Bill.FORMAT_DATE_TIME)
        }
        binding.outAccount.content = outAccount?.name ?: ""
        binding.inAccount.content = inAccount?.name ?: ""
        binding.inPrice.amount = inPrice
        val units = type.name
        binding.outPrice.setUnits(units)
        binding.charges.setUnits(units)
        binding.inPrice.setUnits(units)
        binding.remark.content = bill?.remark ?: ""
        initEnable()
        myListener()
    }

    private fun initEnable() {
        setEnabled(mode != MODE_CHECK, binding.time, binding.outAccount, binding.inAccount, binding.outPrice,
            binding.charges, binding.remark)
    }

    private fun myListener() {
        binding.outAccount.setOnClickListener {
            context?.showToast("选择转出账户")
        }

        binding.inAccount.setOnClickListener {
            context?.showToast("选择转入账户")
        }

        binding.outPrice.setOnClickListener { // 转出金额
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                title.text = getString(R.string.outPrice)
                input.initParams(outPrice)
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        outPrice = BigDecimal(value)
                        binding.outPrice.amount = value
                        if (charges > outPrice) { // 手续费超出转出金额，将手续费置为0
                            charges = BigDecimal.ZERO
                            binding.charges.amount = Bill.ZERO_2
                        }
                        // 转入金额 = 转出金额 - 手续费
                        inPrice = Bill.formatBigDecimal2(outPrice.subtract(charges))
                        binding.inPrice.amount = inPrice
                    }
                })
            }
        }

        binding.charges.setOnClickListener { // 手续费
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                title.text = getString(R.string.charges)
                input.initParams(charges)
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        val temp = BigDecimal(value)
                        if (temp > outPrice) { // 手续费不应超过转出金额
                            context?.showToast("手续费应当不大于转出金额")
                            return
                        }
                        inputAmountDialog.dismiss()
                        // 手续费
                        charges = temp
                        binding.charges.amount = value
                        // 转入金额 = 转出金额 - 手续费
                        inPrice = Bill.formatBigDecimal2(outPrice.subtract(charges))
                        binding.inPrice.amount = inPrice
                    }
                })
            }
        }
    }

    override fun updateBill() {
        bill = Transfer(
            outAccount?.id ?: Bill.NULL_ID,
            inAccount?.id ?: Bill.NULL_ID,
            type.id,
            binding.outPrice.amount,
            binding.charges.amount,
            binding.remark.getContentOrNull(),
            binding.time.getDateTime(Bill.FORMAT_DATE_TIME),
            bill?.dataId ?: Bill.NULL_ID
        )
    }

    override fun onDispatchActionDown(x: Float, y: Float) {
        if (!isHidden) {
            binding.remark.clearFocusWhenParentDispatchDown(x, y)
        }
    }

    companion object {
        const val TAG = Transfer.typeId.toString()

        /**
         * 必须检查[typeSet]不为空集合，可以检查[accountSet]不为空集合.
         */
        @JvmStatic
        fun newInstance(bill: Transfer? = null, mode: Boolean? = MODE_ADD) =
            BillTransferFragment().apply { initArguments(bill, mode) }
    }
}