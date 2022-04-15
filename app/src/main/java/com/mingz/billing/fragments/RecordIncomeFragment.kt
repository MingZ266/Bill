package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mingz.billing.databinding.FragmentRecordIncomeBinding

class RecordIncomeFragment : Fragment() {
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

        context?.let { //context ->
        }
    }
}