package com.mingz.billing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.R
import com.mingz.billing.databinding.FragmentRecordTransferBinding

class RecordTransferFragment : Fragment() {
    private lateinit var binding: FragmentRecordTransferBinding

    /**
     * 转账.
     */
    companion object {
        @JvmStatic
        fun newInstance() = RecordTransferFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { //context ->
        }
    }
}