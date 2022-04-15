package com.mingz.billing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.R
import com.mingz.billing.databinding.FragmentRecordFundSalesBinding

class RecordFundSalesFragment : Fragment() {
    private lateinit var binding: FragmentRecordFundSalesBinding

    /**
     * 基金卖出.
     */
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

        context?.let { //context ->
        }
    }
}