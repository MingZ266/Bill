package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mingz.billing.databinding.FragmentRecordExpenditureBinding

class RecordExpenditureFragment : Fragment() {
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

        context?.let { //context ->
        }
    }
}