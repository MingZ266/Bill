package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentHomeBillingBinding
import com.mingz.billing.entities.Billing
import java.util.*

class HomeBillingFragment : HomeFragment() {
    private lateinit var binding: FragmentHomeBillingBinding

    companion object {
        @JvmStatic
        fun newInstance() = HomeBillingFragment()
    }

    override fun getTitle(): String = "账单"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        val now = Calendar.getInstance()
        Billing.readMonthBilling(now[Calendar.YEAR], now[Calendar.MONTH] + 1)
    }
}
