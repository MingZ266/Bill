package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillExpenditureBinding
import com.mingz.data.*
import com.mingz.data.bill.Bill
import com.mingz.data.bill.Expenditure
import com.mingz.share.MyLog
import com.mingz.share.showToast
import com.mingz.share.ui.InputAmount
import java.math.BigDecimal

class BillExpenditureFragment : BillFragmentImpl<Expenditure>() {
    private lateinit var binding: FragmentBillExpenditureBinding
    private var subjectPack: SubjectPack? = null // 支出科目
    private var account: Account? = null // 付款账户
    private lateinit var type: Type // 币种
    private lateinit var price: BigDecimal // 实付
    private lateinit var discount: BigDecimal // 优惠
    private lateinit var originalPrice: String // 原价 = 实付 + 优惠

    override fun createMyLog() = MyLog("支出")

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            subjectPack = null
            account = null
            type = typeSet[0]
            price = BigDecimal.ZERO
            discount = BigDecimal.ZERO
            originalPrice = Bill.ZERO_2
        } else {
            subjectPack = findSubjectOut(bill.subject)
            account = findAccount(bill.account)
            type = findType(bill.type) ?: typeSet[0]
            price = BigDecimal(bill.price)
            discount = BigDecimal(bill.discount)
            originalPrice = Bill.formatBigDecimal2(price.add(discount))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillExpenditureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        if (bill == null) {
            binding.time.updateToNowTime()
            binding.price.amount = Bill.ZERO_2
            binding.discount.amount = Bill.ZERO_2
        } else {
            binding.time.setDateTime(bill!!.time, Bill.FORMAT_DATE_TIME)
            binding.price.amount = Bill.formatBigDecimal2(price)
            binding.discount.amount = Bill.formatBigDecimal2(discount)
        }
        binding.subject.content = subjectPack?.subject?.name ?: ""
        binding.account.content = account?.name ?: ""
        binding.originalPrice.amount = originalPrice
        binding.remark.content = bill?.remark ?: ""
        val units = type.name
        binding.price.setUnits(units)
        binding.discount.setUnits(units)
        binding.originalPrice.setUnits(units)
        initEnable()
        myListener()
    }

    private fun initEnable() {
        setEnabled(mode != MODE_CHECK, binding.time, binding.subject, binding.account,
            binding.price, binding.discount, binding.remark)
    }

    private fun myListener() {
        binding.subject.setOnClickListener {
            context?.showToast("选择支出科目")
        }

        binding.account.setOnClickListener {
            context?.showToast("选择账户")
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
                        // 原价 = 金额 + 优惠
                        originalPrice = Bill.formatBigDecimal2(price.add(discount))
                        binding.originalPrice.amount = originalPrice
                    }
                })
            }
        }
        binding.price.setOnClickListener(mClickListener) // 实付金额
        binding.discount.setOnClickListener(mClickListener) // 优惠
    }

    override fun updateBill() {
        bill = Expenditure(
            subjectPack?.subject?.id ?: Bill.NULL_ID,
            account?.id ?: Bill.NULL_ID,
            type.id,
            binding.price.amount,
            binding.discount.amount,
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
        const val TAG = Expenditure.typeId.toString()

        /**
         * 必须检查[typeSet]不为空集合，可以检查[subjectOutSet]、[accountSet]不为空集合.
         */
        @JvmStatic
        fun newInstance(bill: Expenditure? = null, mode: Boolean? = MODE_ADD) =
            BillExpenditureFragment().apply { initArguments(bill, mode) }
    }
}