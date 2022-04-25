package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentRecordExpenditureBinding
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.Tools

class RecordExpenditureFragment : RecordFragment() {
    private lateinit var binding: FragmentRecordExpenditureBinding

    companion object {
        @JvmStatic
        fun newInstance() = RecordExpenditureFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordExpenditureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        binding.account.setSelectItem(DataSource.INSTANCE.accountList)
        binding.price.setOnClickListener {
            Tools.inputAmountOfMoney(context, binding.price.getTitle(),
                binding.price.getAmount()) {
                binding.price.setAmount(it)
            }
        }
        binding.originalPrice.setOnClickListener {
            Tools.inputAmountOfMoney(context, binding.originalPrice.getTitle(),
                binding.originalPrice.getAmount()) {
                binding.originalPrice.setAmount(it)
            }
        }
        binding.discount.setOnClickListener {
            Tools.inputAmountOfMoney(context, binding.discount.getTitle(),
                binding.discount.getAmount()) {
                binding.discount.setAmount(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.time.updateToNowTime()
        binding.account.setContent("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ")
    }

    override fun getTitle(): String = "支出"

    override fun save() {
        val context = context ?: return
        Tools.showToast(context, "保存支出")
    }
}