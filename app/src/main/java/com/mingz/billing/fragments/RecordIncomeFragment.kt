package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentRecordIncomeBinding
import com.mingz.billing.utils.Tools

class RecordIncomeFragment : RecordFragment() {
    private lateinit var binding: FragmentRecordIncomeBinding

    companion object {
        @JvmStatic
        fun newInstance() = RecordIncomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
    }

    override fun getTitle(): String = "收入"

    override fun save() {
        val context = context ?: return
        Tools.showToast(context, "保存收入")
    }
}