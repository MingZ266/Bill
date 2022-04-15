package com.mingz.billing.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.mingz.billing.R
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

    private val dateText: TextView
    private val timeText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_show_date_time, this)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
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
        dateText.text = "$year - " +
                "${month.toString().padStart(2, '0')} - " +
                day.toString().padStart(2, '0')
        timeText.text = "${hour.toString().padStart(2, '0')} : " +
                minute.toString().padStart(2, '0')
    }
}