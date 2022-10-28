package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundPurchaseBinding
import com.mingz.data.bill.FundPurchase
import com.mingz.share.MyLog

class BillFundPurchaseFragment : BillFragmentImpl<FundPurchase>() {
    private lateinit var binding: FragmentBillFundPurchaseBinding

    override fun createMyLog() = MyLog("基金买入")

    override fun initFromBill() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
    }

    override fun updateBill() {
    }

    companion object {
        const val TAG = FundPurchase.typeId.toString()

        @JvmStatic
        fun newInstance(bill: FundPurchase? = null, mode: Boolean? = null) =
            BillFundPurchaseFragment().apply { initArguments(bill, mode) }
    }
}