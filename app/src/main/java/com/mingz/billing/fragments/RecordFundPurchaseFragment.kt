package com.mingz.billing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.R
import com.mingz.billing.databinding.FragmentRecordFundPurchaseBinding

class RecordFundPurchaseFragment : Fragment() {
    private lateinit var binding: FragmentRecordFundPurchaseBinding

    /**
     * 基金买入.
     */
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

        context?.let { //context ->
        }
    }
}