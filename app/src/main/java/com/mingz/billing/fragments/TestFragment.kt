package com.mingz.billing.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mingz.billing.R
import com.mingz.billing.databinding.FragmentTestBinding
import com.mingz.billing.ui.MultilevelListView
import com.mingz.billing.utils.MyLog

class TestFragment : Fragment() {
    private lateinit var binding: FragmentTestBinding

    companion object {
        @JvmStatic
        private val myLog = MyLog(this)

        @JvmStatic
        fun newInstance() = TestFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        binding.showDateTime.updateToNowTime()
    }
}