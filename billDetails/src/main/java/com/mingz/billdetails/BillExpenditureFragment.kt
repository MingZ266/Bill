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
import java.math.BigDecimal

class BillExpenditureFragment : BillFragmentImpl<Expenditure>() {
    private lateinit var binding: FragmentBillExpenditureBinding
    private var subjectPack: SubjectPack? = null
    private var account: Account? = null
    private lateinit var type: Type
    private lateinit var price: BigDecimal
    private lateinit var discount: BigDecimal
    private lateinit var originalPrice: BigDecimal

    override fun createMyLog() = MyLog("支出")

    override fun initFromBill() {
        val bill = bill
        if (bill == null) {
            subjectPack = null
            account = null
            type = typeSet[0]
            price = BigDecimal.ZERO
            discount = BigDecimal.ZERO
            originalPrice = BigDecimal.ZERO
        } else {
            subjectPack = findSubjectOut(bill.subject)
            account = findAccount(bill.account)
            type = findType(bill.type) ?: typeSet[0]
            price = BigDecimal(bill.price)
            discount = BigDecimal(bill.discount)
            originalPrice = price.add(discount)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillExpenditureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        updateMenuItem(binding.menu)
        if (bill == null) {
            binding.time.updateToNowTime()
        } else {
            binding.time.setDateTime(bill!!.time, Bill.FORMAT_DATE_TIME)
        }
        binding.subject.content = subjectPack?.subject?.name ?: ""
        binding.account.content = account?.name ?: ""
        val units = type.name
        binding.price.setUnits(units)
        binding.discount.setUnits(units)
        binding.originalPrice.setUnits(units)
        binding.price.amount = String.format(Bill.FORMAT_2, price)
        binding.discount.amount = String.format(Bill.FORMAT_2, discount)
        binding.originalPrice.amount = String.format(Bill.FORMAT_2, originalPrice)
        binding.remark.content = bill?.remark ?: ""
        myListener()
    }

    private fun myListener() {
        binding.menu.item1.setOnClickListener {
            if (mode == null || mode!!) { // 添加或修改账单，此为“选择币种”
                context?.showToast("选择币种")
            } else { // 查看账单，此为“修改账单”
                context?.showToast("修改账单")
            }
        }

        binding.menu.item2.setOnClickListener {
            if (mode == null || mode!!) { // 添加或修改账单，此为“保存”
                context?.showToast("保存")
            } else { // 查看账单，此为“删除账单”
                context?.showToast("删除账单")
            }
        }

        binding.subject.setOnClickListener {
            context?.showToast("选择支出科目")
        }

        binding.account.setOnClickListener {
            context?.showToast("选择账户")
        }

        binding.price.setOnClickListener {
            context?.showToast("输入金额")
        }

        binding.originalPrice.setOnClickListener {
            context?.showToast("输入原价")
        }

        binding.discount.setOnClickListener {
            context?.showToast("输入优惠")
        }
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
        fun newInstance(bill: Expenditure? = null, mode: Boolean? = null) =
            BillExpenditureFragment().apply { initArguments(bill, mode) }
    }
}