package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillTransferBinding
import com.mingz.data.bill.Transfer
import com.mingz.share.MyLog

class BillTransferFragment : BillFragmentImpl<Transfer>() {
    private lateinit var binding: FragmentBillTransferBinding

    override fun createMyLog() = MyLog("转账")

    override fun initFromBill() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
    }

    override fun updateBill() {
    }

    companion object {
        const val TAG = Transfer.typeId.toString()

        @JvmStatic
        fun newInstance(bill: Transfer? = null, mode: Boolean? = null) =
            BillTransferFragment().apply { initArguments(bill, mode) }
    }
}