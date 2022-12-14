package com.mingz.share.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.mingz.share.DialogPack
import com.mingz.share.R
import com.mingz.share.databinding.DialogDateTimePickerBinding
import com.mingz.share.databinding.LayoutShowDateTimeBinding
import com.mingz.share.setPadding
import com.mingz.share.ui.DateTimePicker.MODE
import java.text.SimpleDateFormat
import java.util.*

class ShowDateTime(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val binding: LayoutShowDateTimeBinding
    @MODE
    private val mode: Int
    private val dataTimePicker: DialogPack<DialogDateTimePickerBinding>

    private var year = 1970
    private var month = 1
    private var day = 1
    private var hour = 0
    private var minute = 0

    init {
        binding = LayoutShowDateTimeBinding.inflate(LayoutInflater.from(context), this)
        // 设置根布局参数
        gravity = Gravity.CENTER_VERTICAL
        setPadding(10.0f, 5.0f)
        setBackgroundResource(R.drawable.bg_fill_when_enable)
        orientation = HORIZONTAL
        // 获取可选参数
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowDateTime)
        try {
            isEnabled = typedArray.getBoolean(R.styleable.ShowDateTime_android_enabled, true)
            mode = typedArray.getInt(R.styleable.ShowDateTime_pickerMode, DateTimePicker.MODE_ALL)
        } finally {
            typedArray.recycle()
        }
        // 设置工作模式
        if (mode == DateTimePicker.MODE_DATE) { // 只显示日期
            // 移除右间距
            val params = binding.dateText.layoutParams
            if (params is LayoutParams) {
                params.marginEnd = 0
                binding.dateText.layoutParams = params
            }
            // 隐藏时间
            binding.timeIcon.visibility = View.GONE
            binding.timeText.visibility = View.GONE
        } else if (mode == DateTimePicker.MODE_TIME) { // 只显示时间
            // 隐藏日期
            binding.dateIcon.visibility = View.GONE
            binding.dateText.visibility = View.GONE
        }
        // 设置日期时间选择底部弹窗
        dataTimePicker = DialogPack(context, object : DialogPack.Creator<DialogDateTimePickerBinding> {
            override fun createBinding() = DialogDateTimePickerBinding.inflate(LayoutInflater.from(context))

            override fun initDialog(dialog: AlertDialog, binding: DialogDateTimePickerBinding) {
                binding.picker.setMode(mode)

                binding.cancel.setOnClickListener { dialog.dismiss() }

                binding.confirm.setOnClickListener {
                    dialog.dismiss()
                    setDateTime(binding.picker.year, binding.picker.month, binding.picker.day,
                        binding.picker.hour, binding.picker.minute)
                }
            }
        }, true)
        // 点击弹出日期时间选择底部弹窗
        setOnClickListener {
            if (isEnabled) {
                dataTimePicker.show()
                dataTimePicker.binding.picker.setDateTime(year, month, day, hour, minute)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
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

    private fun setDateTime(calendar: Calendar) = setDateTime(
        calendar[Calendar.YEAR], calendar[Calendar.MONTH] + 1,
        calendar[Calendar.DAY_OF_MONTH], calendar[Calendar.HOUR_OF_DAY],
        calendar[Calendar.MINUTE]
    )

    /**
     * 复制[view]中的日期时间.
     */
    fun setDateTime(view: ShowDateTime) = setDateTime(view.year, view.month, view.day, view.hour, view.minute)

    /**
     * 按[format]格式解析[dateTime]以设置日期时间.
     */
    fun setDateTime(dateTime: String, format: String) {
        SimpleDateFormat(format, Locale.getDefault()).parse(dateTime)?.let {
            setDateTime(Calendar.getInstance().apply { time = it })
        }
    }

    /**
     * 更新到当前日期时间.
     */
    fun updateToNowTime() = setDateTime(Calendar.getInstance())

    /**
     * 按[format]格式获取日期时间.
     */
    fun getDateTime(format: String): String {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month - 1
        calendar[Calendar.DAY_OF_MONTH] = day
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute
        return SimpleDateFormat(format, Locale.getDefault()).format(calendar.time)
    }
}