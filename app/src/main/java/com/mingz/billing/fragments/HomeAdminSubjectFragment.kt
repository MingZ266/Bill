package com.mingz.billing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.databinding.FragmentHomeAdminSubjectBinding

class HomeAdminSubjectFragment : HomeFragment() {
    private lateinit var binding: FragmentHomeAdminSubjectBinding

    companion object {
        @JvmStatic
        fun newInstance() = HomeAdminSubjectFragment()
    }

    override fun getTitle() = "科目"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeAdminSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
    }
}