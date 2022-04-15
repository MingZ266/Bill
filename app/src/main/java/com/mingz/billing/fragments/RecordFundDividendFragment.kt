package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mingz.billing.databinding.FragmentRecordFundDividendBinding

class RecordFundDividendFragment : Fragment() {
    private lateinit var binding: FragmentRecordFundDividendBinding

    /**
     * 基金红利.
     */
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

        context?.let { //context ->
        }
    }
}