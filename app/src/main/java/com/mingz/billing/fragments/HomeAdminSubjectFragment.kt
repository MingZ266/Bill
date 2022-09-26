package com.mingz.billing.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.billing.activities.EditSubjectActivity
import com.mingz.billing.databinding.FragmentHomeAdminSubjectBinding
import com.mingz.billing.utils.DataSource

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
        binding.expenditure.setOnClickListener {
            EditSubjectActivity.subject = DataSource.expenditureSubject
            startActivity(Intent(context, EditSubjectActivity::class.java))
        }
        binding.income.setOnClickListener {
            EditSubjectActivity.subject = DataSource.incomeSubject
            startActivity(Intent(context, EditSubjectActivity::class.java))
        }
    }
}