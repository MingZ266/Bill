package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundPurchaseBinding
import com.mingz.data.*
import com.mingz.data.bill.Bill
import com.mingz.data.bill.FundPurchase
import com.mingz.share.MyLog
import com.mingz.share.showToast
import com.mingz.share.ui.InputAmount
import java.math.BigDecimal
import java.math.RoundingMode

class BillFundPurchaseFragment : BillFragmentImpl<FundPurchase>() {
    private lateinit var binding: FragmentBillFundPurchaseBinding
    private var account: Account? = null // 付款账户
    private lateinit var type: Type // 币种
    private lateinit var price: BigDecimal // 实付
    private lateinit var discount: BigDecimal // 优惠
    private lateinit var purchasePrice: BigDecimal // 买入金额 = 实付 + 优惠
    private lateinit var confirmVal: BigDecimal // 确认净值
    private lateinit var charges: BigDecimal // 手续费
    private lateinit var confirmPrice: BigDecimal // 确认金额 = 买入金额 - 手续费
    private lateinit var confirmAmount: String // 确认份额 = 确认金额 / 确认净值

    override fun createMyLog() = MyLog("基金买入")

    // 计算确认份额
    private fun getConfirmAmount() = try {
        Bill.formatBigDecimal2(confirmPrice.divide(confirmVal, 2, RoundingMode.HALF_UP))
    } catch (e: ArithmeticException) {
        myLog.i("计算“确认份额”异常: 确认金额=${Bill.formatBigDecimal2(confirmPrice)}，" +
                "确认净值=${Bill.formatBigDecimal4(confirmVal)}")
        myLog.v(e, true)
        Bill.ZERO_2
    }

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            account = null
            type = typeSet[0]
            price = BigDecimal.ZERO
            discount = BigDecimal.ZERO
            purchasePrice = BigDecimal.ZERO
            confirmVal = BigDecimal.ZERO
            charges = BigDecimal.ZERO
            confirmPrice = BigDecimal.ZERO
            confirmAmount = Bill.ZERO_2
        } else {
            account = findAccount(bill.account)
            type = findType(bill.type) ?: typeSet[0]
            price = BigDecimal(bill.price)
            discount = BigDecimal(bill.discount)
            purchasePrice = price.add(discount).setScale(2, RoundingMode.HALF_UP)
            confirmVal = BigDecimal(bill.netVal)
            charges = BigDecimal(bill.charges)
            confirmPrice = purchasePrice.subtract(charges).setScale(2, RoundingMode.HALF_UP)
            confirmAmount = getConfirmAmount()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        val bill = bill
        if (bill == null) {
            binding.fund.content = ""
            binding.purchaseTime.updateToNowTime()
            binding.price.amount = Bill.ZERO_2
            binding.discount.amount = Bill.ZERO_2
            binding.purchasePrice.amount = Bill.ZERO_2
            binding.confirmTime.setDateTime(binding.purchaseTime)
            binding.confirmVal.text = Bill.ZERO_4
            binding.charges.text = Bill.ZERO_2
            binding.confirmPrice.text = Bill.ZERO_2
        } else {
            binding.fund.content = bill.fund
            binding.purchaseTime.setDateTime(bill.time, Bill.FORMAT_DATE_TIME)
            binding.price.amount = Bill.formatBigDecimal2(price)
            binding.discount.amount = Bill.formatBigDecimal2(discount)
            binding.purchasePrice.amount = Bill.formatBigDecimal2(purchasePrice)
            binding.confirmTime.setDateTime(bill.confirmDate, Bill.FORMAT_DATE)
            binding.confirmVal.text = Bill.formatBigDecimal4(confirmVal)
            binding.charges.text = Bill.formatBigDecimal2(charges)
            binding.confirmPrice.text = Bill.formatBigDecimal2(confirmPrice)
        }
        binding.account.content = account?.name ?: ""
        val units = type.name
        binding.price.setUnits(units)
        binding.discount.setUnits(units)
        binding.purchasePrice.setUnits(units)
        binding.confirmAmount.text = confirmAmount
        initEnable()
        myListener()
    }

    private fun initEnable() {
        setEnabled(mode != MODE_CHECK, binding.fund, binding.purchaseTime, binding.account, binding.price,
            binding.discount, binding.confirmTime, binding.confirmVal, binding.charges, binding.remark)
    }

    private fun myListener() {
        binding.fund.setOnClickListener {
            context?.showToast("基金")
        }

        binding.account.setOnClickListener {
            context?.showToast("选择付款账户")
        }

        // 金额和优惠的点击监听
        val mClickListener = View.OnClickListener {
            val isPrice = it.id == R.id.price
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                if (isPrice) {
                    title.text = getString(R.string.price)
                    input.initParams(price)
                } else {
                    title.text = getString(R.string.discount)
                    input.initParams(discount)
                }
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        if (isPrice) {
                            price = BigDecimal(value)
                            binding.price.amount = value
                        } else {
                            discount = BigDecimal(value)
                            binding.discount.amount = value
                        }
                        // 买入金额 = 金额 + 优惠
                        purchasePrice = price.add(discount).setScale(2, RoundingMode.HALF_UP)
                        binding.purchasePrice.amount = Bill.formatBigDecimal2(purchasePrice)
                        // 手续费
                        if (charges > purchasePrice) { // 手续费超出买入金额，手续费置为0
                            charges = BigDecimal.ZERO
                            binding.charges.text = Bill.ZERO_2
                        }
                        updateConfirmPrice()
                    }
                })
            }
        }
        binding.price.setOnClickListener(mClickListener) // 实付金额
        binding.discount.setOnClickListener(mClickListener) // 优惠

        binding.confirmVal.setOnClickListener { // 确认净值
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                title.text = getString(R.string.confirmVal)
                input.initParams(confirmVal, 4)
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        confirmVal = BigDecimal(value)
                        binding.confirmVal.text = Bill.formatBigDecimal4(confirmVal)
                        // 确认份额
                        confirmAmount = getConfirmAmount()
                        binding.confirmAmount.text = confirmAmount
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
                        if (temp > purchasePrice) { // 手续费不应超过买入金额
                            context?.showToast("手续费应当不大于买入金额")
                            return
                        }
                        inputAmountDialog.dismiss()
                        // 手续费
                        charges = temp
                        binding.charges.text = value
                        updateConfirmPrice()
                    }
                })
            }
        }
    }

    // 当“买入金额”或“手续费”变动时应当调用
    private fun updateConfirmPrice() {
        // 确认金额 = 买入金额 - 手续费
        confirmPrice = purchasePrice.subtract(charges).setScale(2, RoundingMode.HALF_UP)
        binding.confirmPrice.text = Bill.formatBigDecimal2(confirmPrice)
        // 确认份额 = 确认金额 / 确认净值
        confirmAmount = getConfirmAmount()
        binding.confirmAmount.text = confirmAmount
    }

    override fun updateBill() {
        bill = FundPurchase(
            binding.fund.content,
            account?.id ?: Bill.NULL_ID,
            type.id,
            binding.price.amount,
            binding.discount.amount,
            binding.confirmTime.getDateTime(Bill.FORMAT_DATE),
            binding.confirmVal.text.toString(),
            binding.charges.text.toString(),
            binding.remark.getContentOrNull(),
            binding.purchaseTime.getDateTime(Bill.FORMAT_DATE_TIME),
            bill?.dataId ?: Bill.NULL_ID
        )
    }

    override fun onDispatchActionDown(x: Float, y: Float) {
        if (!isHidden) {
            binding.remark.clearFocusWhenParentDispatchDown(x, y)
        }
    }

    companion object {
        const val TAG = FundPurchase.typeId.toString()

        /**
         * 必须检查[typeSet]不为空集合，可以检查[accountSet]不为空集合.
         */
        @JvmStatic
        fun newInstance(bill: FundPurchase? = null, mode: Boolean? = MODE_ADD) =
            BillFundPurchaseFragment().apply { initArguments(bill, mode) }
    }
}