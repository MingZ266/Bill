package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mingz.billing.databinding.FragmentMainBillingBinding

class MainBillingFragment : MainFragment() {
    private lateinit var binding: FragmentMainBillingBinding

    companion object {
        @JvmStatic
        fun newInstance() = MainBillingFragment()
    }

    override fun getTitle(): String = "账单"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
    }

    private data class DayData(val data: String)

    private data class MonthData(val data: String, val dayData: List<DayData>)

    private data class YearData(val data: String, val monthData: List<MonthData>)
}
