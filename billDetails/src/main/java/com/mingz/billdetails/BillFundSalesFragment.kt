package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundSalesBinding
import com.mingz.data.bill.FundSales
import com.mingz.share.MyLog

class BillFundSalesFragment : BillFragmentImpl<FundSales>() {
    private lateinit var binding: FragmentBillFundSalesBinding

    override fun createMyLog() = MyLog("基金卖出")

    override fun initFromBill() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
    }

    override fun updateBill() {
    }

    companion object {
        const val TAG = FundSales.typeId.toString()

        @JvmStatic
        fun newInstance(bill: FundSales? = null, mode: Boolean? = null) =
            BillFundSalesFragment().apply { initArguments(bill, mode) }
    }
}