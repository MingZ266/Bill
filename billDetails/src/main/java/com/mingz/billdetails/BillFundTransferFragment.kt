package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundTransferBinding
import com.mingz.data.*
import com.mingz.data.bill.Bill
import com.mingz.data.bill.FundTransfer
import com.mingz.share.MyLog
import com.mingz.share.showToast
import com.mingz.share.ui.InputAmount
import java.math.BigDecimal
import java.math.RoundingMode

class BillFundTransferFragment : BillFragmentImpl<FundTransfer>() {
    private lateinit var binding: FragmentBillFundTransferBinding
    private lateinit var type: Type // 币种
    private lateinit var outAmount: BigDecimal // 转出基金份额
    private lateinit var outNetVal: BigDecimal // 转出基金净值
    private lateinit var outPrice: BigDecimal // 转出金额 = 转出基金份额 * 转出基金净值
    private lateinit var charges: BigDecimal // 手续费
    private lateinit var inAmount: BigDecimal // 转入基金份额
    private lateinit var inNetVal: BigDecimal // 转入基金净值
    private lateinit var inPrice: BigDecimal // 转入金额 = 转入基金份额 * 转入基金净值
    // 尾差
    private var account: Account? = null // 动账账户
    private lateinit var priceForAccount: String // 动账金额 = 转出金额 - 转入金额 - 手续费
    private lateinit var actualPrice: BigDecimal // 实际动账金额

    override fun createMyLog() = MyLog("基金转换")

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            type = typeSet[0]
            outAmount = BigDecimal.ZERO
            outNetVal = BigDecimal.ZERO
            outPrice = BigDecimal.ZERO
            charges = BigDecimal.ZERO
            inAmount = BigDecimal.ZERO
            inNetVal = BigDecimal.ZERO
            inPrice = BigDecimal.ZERO
            account = null
            priceForAccount = Bill.ZERO_2
            actualPrice = BigDecimal.ZERO
        } else {
            type = findType(bill.type) ?: typeSet[0]
            outAmount = BigDecimal(bill.outAmount)
            outNetVal = BigDecimal(bill.outNetVal)
            outPrice = outAmount.multiply(outNetVal).setScale(2, RoundingMode.HALF_UP)
            charges = BigDecimal(bill.charges)
            inAmount = BigDecimal(bill.inAmount)
            inNetVal = BigDecimal(bill.inNetVal)
            inPrice = inAmount.multiply(inNetVal).setScale(2, RoundingMode.HALF_UP)
            account = findAccount(bill.account)
            priceForAccount = Bill.formatBigDecimal2(outPrice.subtract(inPrice).subtract(charges))
            val billActualPrice = bill.price
            actualPrice = if (billActualPrice == null) BigDecimal.ZERO else BigDecimal(billActualPrice)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        val bill = bill
        if (bill == null) {
            binding.transferTime.updateToNowTime()
            binding.outFund.content = ""
            binding.outAmount.text = Bill.ZERO_2
            binding.outNetVal.text = Bill.ZERO_4
            binding.outPrice.text = Bill.ZERO_2
            binding.charges.amount = Bill.ZERO_2
            binding.inFund.content = ""
            binding.inAmount.text = Bill.ZERO_2
            binding.inNetVal.text = Bill.ZERO_4
            binding.inPrice.text = Bill.ZERO_2
            binding.timeForAccount.setDateTime(binding.transferTime)
            binding.actualPrice.amount = Bill.ZERO_2
        } else {
            binding.transferTime.setDateTime(bill.time, Bill.FORMAT_DATE_TIME)
            binding.outFund.content = bill.outFund
            binding.outAmount.text = Bill.formatBigDecimal2(outAmount)
            binding.outNetVal.text = Bill.formatBigDecimal4(outNetVal)
            binding.outPrice.text = Bill.formatBigDecimal2(outPrice)
            binding.charges.amount = Bill.formatBigDecimal2(charges)
            binding.inFund.content = bill.inFund
            binding.inAmount.text = Bill.formatBigDecimal2(inAmount)
            binding.inNetVal.text = Bill.formatBigDecimal4(inNetVal)
            binding.inPrice.text = Bill.formatBigDecimal2(inPrice)
            val billTimeForAccount = bill.timeForAccount
            if (billTimeForAccount == null) {
                binding.timeForAccount.setDateTime(binding.transferTime)
            } else {
                binding.timeForAccount.setDateTime(billTimeForAccount, Bill.FORMAT_DATE_TIME)
            }
            binding.actualPrice.amount = Bill.formatBigDecimal2(actualPrice)
        }
        val units = type.name
        binding.charges.setUnits(units)
        binding.priceForAccount.setUnits(units)
        binding.actualPrice.setUnits(units)
        binding.account.content = account?.name ?: ""
        binding.priceForAccount.amount = priceForAccount
        initEnable()
        myListener()
    }

    private fun initEnable() {
        if (mode == MODE_CHECK) { // 查看账单
            binding.needAccount.visibility = if (bill == null || bill!!.price != null) View.VISIBLE else View.GONE
        } else { // 添加账单或修改账单
            binding.needAccount.visibility = View.VISIBLE
            updateAccountEnable()
        }
        setEnabled(mode != MODE_CHECK, binding.transferTime, binding.outFund, binding.outAmount,
            binding.outNetVal, binding.charges, binding.inFund, binding.inAmount, binding.inNetVal,
            binding.actualPrice, binding.remark)
    }

    private fun updateAccountEnable() {
        setEnabled(actualPrice.compareTo(BigDecimal.ZERO) != 0, // 实际动账金额不为0
            binding.timeForAccount, binding.account)
    }

    private fun myListener() {
        binding.outFund.setOnClickListener {
            context?.showToast("转出基金")
        }

        binding.inFund.setOnClickListener {
            context?.showToast("转入基金")
        }

        binding.account.setOnClickListener {
            context?.showToast("选择账户")
        }

        val mClickListener = View.OnClickListener { v ->
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                when (v.id) {
                    R.id.outAmount -> {
                        title.text = getString(R.string.outAmount)
                        input.initParams(outAmount)
                    }
                    R.id.outNetVal -> {
                        title.text = getString(R.string.fundNetVal)
                        input.initParams(outNetVal, 4)
                    }
                    R.id.inAmount -> {
                        title.text = getString(R.string.inAmount)
                        input.initParams(inAmount)
                    }
                    R.id.inNetVal -> {
                        title.text = getString(R.string.fundNetVal)
                        input.initParams(inNetVal, 4)
                    }
                }
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        when (v.id) {
                            R.id.outAmount -> { // 转出基金份额
                                outAmount = BigDecimal(value)
                                binding.outAmount.text = value
                                // 更新转出金额
                                outPrice = outAmount.multiply(outNetVal).setScale(2, RoundingMode.HALF_UP)
                                binding.outPrice.text = Bill.formatBigDecimal2(outPrice)
                            }
                            R.id.outNetVal -> { // 转出基金净值
                                outNetVal = BigDecimal(value)
                                binding.outNetVal.text = value
                                // 更新转出金额
                                outPrice = outAmount.multiply(outNetVal).setScale(2, RoundingMode.HALF_UP)
                                binding.outPrice.text = Bill.formatBigDecimal2(outPrice)
                            }
                            R.id.inAmount -> { // 转入基金份额
                                inAmount = BigDecimal(value)
                                binding.inAmount.text = value
                                // 更新转入金额
                                inPrice = inAmount.multiply(inNetVal).setScale(2, RoundingMode.HALF_UP)
                                binding.inPrice.text = Bill.formatBigDecimal2(inPrice)
                            }
                            R.id.inNetVal -> { // 转入基金净值
                                inNetVal = BigDecimal(value)
                                binding.inNetVal.text = value
                                // 更新转入金额
                                inPrice = inAmount.multiply(inNetVal).setScale(2, RoundingMode.HALF_UP)
                                binding.inPrice.text = Bill.formatBigDecimal2(inPrice)
                            }
                        }
                        updatePriceForAccount()
                    }
                })
            }
        }
        binding.outAmount.setOnClickListener(mClickListener) // 转出基金份额
        binding.outNetVal.setOnClickListener(mClickListener) // 转出基金净值
        binding.inAmount.setOnClickListener(mClickListener) // 转入基金份额
        binding.inNetVal.setOnClickListener(mClickListener) // 转入基金净值

        binding.charges.setOnClickListener { // 手续费
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                title.text = getString(R.string.charges)
                input.initParams(charges)
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        charges = BigDecimal(value)
                        binding.charges.amount = value
                        updatePriceForAccount()
                    }
                })
            }
        }

        binding.actualPrice.setOnClickListener { // 实际动账金额
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                title.text = getString(R.string.actualPrice)
                input.initParams(actualPrice, allowNeg = true)
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        actualPrice = BigDecimal(value)
                        binding.actualPrice.amount = value
                        updateAccountEnable()
                    }
                })
            }
        }
    }

    // 当“转出金额”或“转入金额”或“手续费”改变时调用
    private fun updatePriceForAccount() {
        actualPrice = outPrice.subtract(inPrice).subtract(charges).setScale(2, RoundingMode.HALF_UP)
        priceForAccount = Bill.formatBigDecimal2(actualPrice)
        binding.actualPrice.amount = priceForAccount
        binding.priceForAccount.amount = priceForAccount
        updateAccountEnable()
    }

    override fun updateBill() {
        bill = FundTransfer(
            binding.outFund.content,
            binding.outAmount.text.toString(),
            binding.outNetVal.text.toString(),
            binding.charges.amount,
            binding.inFund.content,
            binding.inAmount.text.toString(),
            binding.inNetVal.text.toString(),
            binding.timeForAccount.getDateTime(Bill.FORMAT_DATE_TIME),
            account?.id,
            type.id,
            binding.actualPrice.amount,
            binding.remark.getContentOrNull(),
            binding.transferTime.getDateTime(Bill.FORMAT_DATE_TIME),
            bill?.dataId ?: Bill.NULL_ID
        )
    }

    override fun onDispatchActionDown(x: Float, y: Float) {
        if (!isHidden) {
            binding.remark.clearFocusWhenParentDispatchDown(x, y)
        }
    }

    companion object {
        const val TAG = FundTransfer.typeId.toString()

        /**
         * 必须检查[typeSet]不为空集合，可以检查[accountSet]不为空集合.
         */
        @JvmStatic
        fun newInstance(bill: FundTransfer? = null, mode: Boolean? = MODE_ADD) =
            BillFundTransferFragment().apply { initArguments(bill, mode) }
    }
}