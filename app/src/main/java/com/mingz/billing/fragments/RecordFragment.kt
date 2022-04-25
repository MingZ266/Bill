package com.mingz.billing.fragments

import androidx.fragment.app.Fragment

abstract class RecordFragment : Fragment() {
    abstract fun getTitle(): String

    abstract fun save()
}