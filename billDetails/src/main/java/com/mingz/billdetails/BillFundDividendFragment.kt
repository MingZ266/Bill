package com.mingz.billdetails

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundDividendBinding
import com.mingz.data.*
import com.mingz.data.bill.Bill
import com.mingz.data.bill.FundDividend
import com.mingz.share.MyLog
import com.mingz.share.showToast
import com.mingz.share.ui.InputAmount
import java.math.BigDecimal
import java.math.RoundingMode

class BillFundDividendFragment : BillFragmentImpl<FundDividend>() {
    private lateinit var binding: FragmentBillFundDividendBinding
    private lateinit var whenRedo: Array<View>
    private lateinit var price: BigDecimal // 金额
    private lateinit var type: Type // 币种
    private var redo = false // 是否再投资
    // 否再投资
    private var account: Account? = null // 收款账户
    // 再投资
    private lateinit var netVal: BigDecimal // 基金净值
    private lateinit var amount: String // 增加份额 = 金额 / 基金净值

    override fun createMyLog() = MyLog("基金分红")

    // 计算增加份额
    private fun getAmount() = try {
        Bill.formatBigDecimal2(price.divide(netVal, 2, RoundingMode.HALF_UP))
    } catch (e: ArithmeticException) {
        myLog.i("计算“增加份额”异常: 金额=${Bill.formatBigDecimal2(price)}, 基金净值=${Bill.formatBigDecimal4(netVal)}")
        myLog.v(e, true)
        Bill.ZERO_2
    }

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            price = BigDecimal.ZERO
            type = typeSet[0]
            redo = false
            account = null
            netVal = BigDecimal.ZERO
            amount = Bill.ZERO_2
        } else {
            price = BigDecimal(bill.price)
            type = findType(bill.type) ?: typeSet[0]
            redo = bill.redo
            account = findAccount(bill.account)
            val billNetVal = bill.netVal
            netVal = if (billNetVal == null) BigDecimal.ZERO else BigDecimal(billNetVal)
            amount = getAmount()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundDividendBinding.inflate(inflater, container, false)
        whenRedo = arrayOf(binding.useless1, binding.useless2, binding.netVal, binding.incrementAmount)
        return binding.root
    }

    override fun initView() {
        val bill = bill
        if (bill == null) {
            binding.time.updateToNowTime()
            binding.fund.content = ""
            binding.price.amount = Bill.ZERO_2
            binding.netVal.text = Bill.ZERO_4
        } else {
            binding.time.setDateTime(bill.time, Bill.FORMAT_DATE_TIME)
            binding.fund.content = bill.fund
            binding.price.amount = Bill.formatBigDecimal2(price)
            binding.netVal.text = Bill.formatBigDecimal4(netVal)
        }
        binding.price.setUnits(type.name)
        binding.redo.isChecked = redo
        binding.account.content = account?.name ?: ""
        binding.incrementAmount.text = amount
        initEnable()
        myListener()
    }

    private fun initEnable() {
        setEnabled(mode != MODE_CHECK, binding.time, binding.fund, binding.price, binding.redo,
            binding.account, binding.netVal, binding.remark)
    }

    private fun myListener() {
        binding.fund.setOnClickListener {
            context?.showToast("基金")
        }

        var isChecked = false
        val alphaAnimation = ValueAnimator.ofFloat(0.0f, 1.0f)
        alphaAnimation.duration = 300
        alphaAnimation.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            private var notDone = true // 是否尚未执行过变更可见性

            override fun onAnimationUpdate(animation: ValueAnimator) {
                val value = animation.animatedValue as Float
                if (isChecked) { // “收款账户”逐渐透明，完全透明后，“whenRedo”将逐渐变得不透明
                    if (value < 0.5f) { // “收款账户”逐渐透明
                        notDone = true
                        binding.account.alpha = 1.0f - value * 2.0f
                    } else { // “whenRedo”逐渐不透明
                        if (notDone) { // 变更可见性
                            notDone = false
                            binding.account.visibility = View.GONE
                            setWhenRedoAlpha(0.0f) // 从透明开始变化
                            binding.whenRedo.visibility = View.VISIBLE
                        }
                        setWhenRedoAlpha((value - 0.5f) * 2.0f)
                    }
                } else { // 逆向过程
                    if (value < 0.5f) { // “whenRedo”逐渐透明
                        notDone = true
                        setWhenRedoAlpha(1.0f - value * 2.0f)
                    } else { // “收款账户”逐渐不透明
                        if (notDone) { // 变更可见性
                            notDone = false
                            binding.whenRedo.visibility = View.GONE
                            binding.account.alpha = 0.0f // 从透明开始变化
                            binding.account.visibility = View.VISIBLE
                        }
                        binding.account.alpha = (value - 0.5f) * 2.0f
                    }
                }
            }
        })
        binding.redo.setOnCheckedChangeListener { _, checked ->
            isChecked = checked
            alphaAnimation.start()
        }

        binding.account.setOnClickListener {
            context?.showToast("选择收款账户")
        }

        // 金额和基金净值的点击监听
        val mClickListener = View.OnClickListener {
            val isPrice = it.id == R.id.price
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                if (isPrice) {
                    title.text = getString(R.string.price)
                    input.initParams(price)
                } else {
                    title.text = getString(R.string.fundNetVal)
                    input.initParams(netVal, 4)
                }
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        if (isPrice) {
                            price = BigDecimal(value)
                            binding.price.amount = value
                        } else {
                            netVal = BigDecimal(value)
                            binding.netVal.text = value
                        }
                        // 增加份额
                        amount = getAmount()
                        binding.incrementAmount.text = amount
                    }
                })
            }
        }
        binding.price.setOnClickListener(mClickListener) // 金额
        binding.netVal.setOnClickListener(mClickListener) // 基金净值
    }

    private fun setWhenRedoAlpha(alpha: Float) {
        for (v in whenRedo) {
            v.alpha = alpha
        }
    }

    override fun updateBill() {
        bill = FundDividend(
            binding.fund.content,
            binding.price.amount,
            binding.redo.isChecked,
            account?.id,
            type.id,
            binding.netVal.text.toString(),
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
        const val TAG = FundDividend.typeId.toString()

        /**
         * 必须检查[typeSet]不为空集合，可以检查[accountSet]不为空集合.
         */
        @JvmStatic
        fun newInstance(bill: FundDividend? = null, mode: Boolean? = MODE_ADD) =
            BillFundDividendFragment().apply { initArguments(bill, mode) }
    }
}