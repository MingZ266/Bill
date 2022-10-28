package com.mingz.billdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billdetails.databinding.FragmentBillFundDividendBinding
import com.mingz.data.bill.FundDividend
import com.mingz.share.MyLog

class BillFundDividendFragment : BillFragmentImpl<FundDividend>() {
    private lateinit var binding: FragmentBillFundDividendBinding

    override fun createMyLog() = MyLog("基金分红")

    override fun initFromBill() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillFundDividendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        myListener()
    }

    private fun myListener() {
        binding.redo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.whenRedo.visibility = View.VISIBLE
                binding.account.visibility = View.GONE
            } else {
                binding.whenRedo.visibility = View.GONE
                binding.account.visibility = View.VISIBLE
            }
        }

        binding.fund.setOnClickListener {
            binding.redo.isEnabled = !binding.redo.isEnabled
        }
    }

    override fun updateBill() {
    }

    companion object {
        const val TAG = FundDividend.typeId.toString()

        @JvmStatic
        fun newInstance(bill: FundDividend? = null, mode: Boolean? = null) =
            BillFundDividendFragment().apply { initArguments(bill, mode) }
    }
}