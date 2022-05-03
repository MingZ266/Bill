package com.mingz.billing.fragments

import androidx.fragment.app.Fragment

abstract class HomeFragment : Fragment() {
    abstract fun getTitle(): String
}