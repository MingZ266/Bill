package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentRecordFundPurchaseBinding
import com.mingz.billing.utils.Tools

class RecordFundPurchaseFragment : RecordFragment() {
    private lateinit var binding: FragmentRecordFundPurchaseBinding

    companion object {
        @JvmStatic
        fun newInstance() = RecordFundPurchaseFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordFundPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        binding.discount.setOnClickListener {
            Tools.inputAmountOfMoney(context, binding.discount.getTitle(),
                binding.discount.getAmount()) {
                binding.discount.setAmount(it)
            }
        }
        binding.confirmValue.setOnClickListener {
            Tools.inputAmountOfMoney(context, "确认净值",
                binding.confirmValue.text.toString(), 4) {
                binding.confirmValue.text = it
            }
        }
        // 测试
        binding.price.setOnClickListener {
            Tools.inputAmountOfMoney(context, "测试负值",
                binding.price.getAmount(),
                2, true) {
                binding.price.setAmount(it)
            }
        }
    }

    override fun getTitle(): String = "买入"

    override fun save() {
        val context = context ?: return
        Tools.showToast(context, "基金买入")
    }
}