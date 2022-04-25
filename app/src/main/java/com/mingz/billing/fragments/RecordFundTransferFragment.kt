package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentRecordFundTransferBinding
import com.mingz.billing.utils.Tools

class RecordFundTransferFragment : RecordFragment() {
    private lateinit var binding: FragmentRecordFundTransferBinding

    companion object {
        @JvmStatic
        fun newInstance() = RecordFundTransferFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordFundTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
    }

    override fun getTitle(): String = "转换"

    override fun save() {
        val context = context ?: return
        Tools.showToast(context, "保存基金转换")
    }
}