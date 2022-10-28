package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundTransferBinding
import com.mingz.data.bill.FundTransfer
import com.mingz.share.MyLog

class BillFundTransferFragment : BillFragmentImpl<FundTransfer>() {
    private lateinit var binding: FragmentBillFundTransferBinding

    override fun createMyLog() = MyLog("基金转换")

    override fun initFromBill() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
    }

    override fun updateBill() {
    }

    companion object {
        const val TAG = FundTransfer.typeId.toString()

        @JvmStatic
        fun newInstance(bill: FundTransfer? = null, mode: Boolean? = null) =
            BillFundTransferFragment().apply { initArguments(bill, mode) }
    }
}