package com.mingz.billing.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mingz.billing.R
import java.util.*

class ShowDateTime(context: Context, attrs: AttributeSet? = null)
    : LinearLayout(context, attrs) {
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
        val oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f,
            context.resources.displayMetrics)
        val paddingSize = (5.0 * oneDp).toInt()
        val picSize = (30.0 * oneDp).toInt()
        val leftMargin = (10.0 * oneDp).toInt()
        val textSize = 18.0f
        // 设置父组件参数
        orientation = HORIZONTAL
        setPadding(paddingLeft, paddingSize, paddingRight, paddingSize)
        gravity = Gravity.CENTER_VERTICAL
        // 添加子组件
        val dateIcon = ImageView(context)
        dateIcon.layoutParams = LayoutParams(picSize, picSize)
        dateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_date))
        addView(dateIcon)

        dateText = TextView(context)
        val dateParams  = LayoutParams(0, LayoutParams.WRAP_CONTENT, 3.0f)
        dateParams.leftMargin = leftMargin
        dateText.layoutParams = dateParams
        dateText.textSize = textSize
        addView(dateText)

        val timeIcon = ImageView(context)
        timeIcon.layoutParams = LayoutParams(picSize, picSize)
        timeIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_time))
        addView(timeIcon)

        timeText = TextView(context)
        val timeParams  = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2.0f)
        timeParams.leftMargin = leftMargin
        timeText.layoutParams = timeParams
        timeText.textSize = textSize
        addView(timeText)
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