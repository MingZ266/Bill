package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentRecordFundSalesBinding
import com.mingz.billing.utils.Tools

class RecordFundSalesFragment : RecordFragment() {
    private lateinit var binding: FragmentRecordFundSalesBinding

    companion object {
        @JvmStatic
        fun newInstance() = RecordFundSalesFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordFundSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
    }

    override fun getTitle(): String = "卖出"

    override fun save() {
        val context = context ?: return
        Tools.showToast(context, "保存基金卖出")
    }
}