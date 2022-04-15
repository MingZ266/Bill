package com.mingz.billing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.R

class BillingMainFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = BillingMainFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main_billing, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { //context ->
            initView(view)
        }
    }

    private fun initView(view: View) {
    }

    private data class DayData(val data: String)

    private data class MonthData(val data: String, val dayData: List<DayData>)

    private data class YearData(val data: String, val monthData: List<MonthData>)
}
