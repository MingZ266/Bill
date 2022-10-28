package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillIncomeBinding
import com.mingz.data.bill.Income
import com.mingz.share.MyLog

class BillIncomeFragment : BillFragmentImpl<Income>() {
    private lateinit var binding: FragmentBillIncomeBinding

    override fun createMyLog() = MyLog("收入")

    override fun initFromBill() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
    }

    override fun updateBill() {
    }

    companion object {
        const val TAG = Income.typeId.toString()

        @JvmStatic
        fun newInstance(bill: Income? = null, mode: Boolean? = null) =
            BillIncomeFragment().apply { initArguments(bill, mode) }
    }
}