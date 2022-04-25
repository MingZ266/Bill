package com.mingz.billing.fragments

import androidx.fragment.app.Fragment

abstract class MainFragment : Fragment() {
    abstract fun getTitle(): String
}