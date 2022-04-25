package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentRecordFundDividendBinding
import com.mingz.billing.utils.Tools

class RecordFundDividendFragment : RecordFragment() {
    private lateinit var binding: FragmentRecordFundDividendBinding

    companion object {
        @JvmStatic
        fun newInstance() = RecordFundDividendFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordFundDividendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
    }

    override fun getTitle(): String = "红利"

    override fun save() {
        val context = context ?: return
        Tools.showToast(context, "保存基金红利")
    }
}