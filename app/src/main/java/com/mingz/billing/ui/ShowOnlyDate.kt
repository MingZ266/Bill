package com.mingz.billing.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.mingz.billing.R

class ShowOnlyDate(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {
    var year = 1970
        private set
    var month = 1
        private set
    var day = 1
        private set

    private val dateText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_show_only_date, this)
        dateText = findViewById(R.id.dateText)
    }

    @SuppressLint("SetTextI18n")
    fun setTime(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
        dateText.text = "$year - " +
                "${month.toString().padStart(2, '0')} - " +
                day.toString().padStart(2, '0')
    }

    fun setTime(showDateTime: ShowDateTime) =
        setTime(showDateTime.year, showDateTime.month, showDateTime.day)
}