package com.mingz.billing.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.mingz.billing.databinding.DialogDateTimePickerBinding
import com.mingz.billing.databinding.LayoutShowOnlyDateBinding
import com.mingz.billing.utils.Tools

class ShowOnlyDate(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {
    var year = 1970
        private set
    var month = 1
        private set
    var day = 1
        private set

    private val binding: LayoutShowOnlyDateBinding

    init {
        binding = LayoutShowOnlyDateBinding.inflate(LayoutInflater.from(context),
            this, true)
        setListener()
    }

    @SuppressLint("SetTextI18n")
    fun setTime(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
        binding.dateText.text = "$year - " +
                "${month.toString().padStart(2, '0')} - " +
                day.toString().padStart(2, '0')
    }

    fun setTime(showDateTime: ShowDateTime) =
        setTime(showDateTime.year, showDateTime.month, showDateTime.day)

    private fun setListener() = setOnClickListener {
        val binding = DialogDateTimePickerBinding.inflate(LayoutInflater.from(context))
        val dialog = Tools.showBottomPopup(context, binding.root)
        dialog.setCanceledOnTouchOutside(false)
        binding.picker.setMode(DateTimePicker.MODE_DATE)
        binding.picker.setDate(year, month, day)
        binding.cancel.setOnClickListener { dialog.cancel() }
        binding.confirm.setOnClickListener {
            setTime(binding.picker.year, binding.picker.month, binding.picker.day)
            dialog.cancel()
        }
    }
}