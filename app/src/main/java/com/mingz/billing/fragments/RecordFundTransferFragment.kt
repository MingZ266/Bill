package com.mingz.billing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.R
import com.mingz.billing.databinding.FragmentRecordFundTransferBinding

class RecordFundTransferFragment : Fragment() {
    private lateinit var binding: FragmentRecordFundTransferBinding

    /**
     * 基金转换.
     */
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

        context?.let { //context ->
        }
    }
}