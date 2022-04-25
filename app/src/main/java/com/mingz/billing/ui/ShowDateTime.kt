package com.mingz.billing.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.mingz.billing.R
import com.mingz.billing.databinding.DialogDateTimePickerBinding
import com.mingz.billing.databinding.LayoutShowDateTimeBinding
import com.mingz.billing.utils.Tools
import java.util.*

class ShowDateTime(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {
    var year = 1970
        private set
    var month = 1
        private set
    var day = 1
        private set
    var hour = 8
        private set
    var minute = 0
        private set

    private val binding: LayoutShowDateTimeBinding

    init {
        binding = LayoutShowDateTimeBinding.inflate(LayoutInflater.from(context),
            this, true)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowDateTime)
        try {
            binding.root.isEnabled = typedArray.getBoolean(R.styleable.ShowDateTime_android_enabled, true)
        } finally {
            typedArray.recycle()
        }
        setListener()
    }

    fun updateToNowTime() {
        val now = Calendar.getInstance()
        setTime(
            now[Calendar.YEAR], now[Calendar.MONTH] + 1,
            now[Calendar.DAY_OF_MONTH], now[Calendar.HOUR_OF_DAY],
            now[Calendar.MINUTE]
        )
    }

    @SuppressLint("SetTextI18n")
    fun setTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        this.year = year
        this.month = month
        this.day = day
        this.hour = hour
        this.minute = minute
        binding.dateText.text = "$year - " +
                "${month.toString().padStart(2, '0')} - " +
                day.toString().padStart(2, '0')
        binding.timeText.text = "${hour.toString().padStart(2, '0')} : " +
                minute.toString().padStart(2, '0')
    }

    override fun setEnabled(enabled: Boolean) {
        binding.root.isEnabled = enabled
    }

    private fun setListener() = setOnClickListener {
        if (this.binding.root.isEnabled) {
            val binding = DialogDateTimePickerBinding.inflate(LayoutInflater.from(context))
            val dialog = Tools.showBottomPopup(context, binding.root)
            dialog.setCanceledOnTouchOutside(false)
            binding.picker.setDateTime(year, month, day, hour, minute)
            binding.cancel.setOnClickListener { dialog.cancel() }
            binding.confirm.setOnClickListener {
                setTime(binding.picker.year, binding.picker.month, binding.picker.day,
                    binding.picker.hour, binding.picker.minute)
                dialog.cancel()
            }
        }
    }
}