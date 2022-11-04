package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillIncomeBinding
import com.mingz.data.*
import com.mingz.data.bill.Bill
import com.mingz.data.bill.Income
import com.mingz.share.MyLog
import com.mingz.share.showToast
import com.mingz.share.ui.InputAmount

class BillIncomeFragment : BillFragmentImpl<Income>() {
    private lateinit var binding: FragmentBillIncomeBinding
    private var subjectPack: SubjectPack? = null // 收入科目
    private var account: Account? = null // 收款账户
    private lateinit var type: Type // 币种
    private lateinit var price: String // 金额

    override fun createMyLog() = MyLog("收入")

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            subjectPack = null
            account = null
            type = typeSet[0]
            price = Bill.ZERO_2
        } else {
            subjectPack = findSubjectIn(bill.subject)
            account = findAccount(bill.account)
            type = findType(bill.type) ?: typeSet[0]
            price = bill.price
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        if (bill == null) {
            binding.time.updateToNowTime()
        } else {
            binding.time.setDateTime(bill!!.time, Bill.FORMAT_DATE_TIME)
        }
        binding.subject.content = subjectPack?.subject?.name ?: ""
        binding.account.content = account?.name ?: ""
        binding.price.amount = price
        binding.price.setUnits(type.name)
        binding.remark.content = bill?.remark ?: ""
        initEnable()
        myListener()
    }

    private fun initEnable() {
        setEnabled(mode != MODE_CHECK, binding.time, binding.subject, binding.account, binding.price,
            binding.remark)
    }

    private fun myListener() {
        binding.subject.setOnClickListener {
            context?.showToast("选择收入科目")
        }

        binding.account.setOnClickListener {
            context?.showToast("选择账户")
        }

        binding.price.setOnClickListener { // 金额
            inputAmountDialog.show()
            with(inputAmountDialog.binding) {
                title.text = getString(R.string.price)
                input.initParams(price)
                input.setInputListener(object : InputAmount.InputListener {
                    override fun onOk(value: String) {
                        inputAmountDialog.dismiss()
                        price = value
                        binding.price.amount = value
                    }
                })
            }
        }
    }

    override fun updateBill() {
        bill = Income(
            subjectPack?.subject?.id ?: Bill.NULL_ID,
            account?.id ?: Bill.NULL_ID,
            type.id,
            price,
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
        const val TAG = Income.typeId.toString()

        /**
         * 必须检查[typeSet]不为空集合，可以检查[subjectInSet]、[accountSet]不为空集合.
         */
        @JvmStatic
        fun newInstance(bill: Income? = null, mode: Boolean? = MODE_ADD) =
            BillIncomeFragment().apply { initArguments(bill, mode) }
    }
}