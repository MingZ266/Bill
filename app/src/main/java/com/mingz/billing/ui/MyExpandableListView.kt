package com.mingz.billing.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ExpandableListView
import com.mingz.billing.utils.MyLog

class MyExpandableListView constructor(
    context: Context, attrs: AttributeSet? = null
) : ExpandableListView(context, attrs) {
    private val myLog = MyLog(this)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        myLog.v("${hashCode()}: before onMeasure")
        super.onMeasure(widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST))
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        myLog.v("${hashCode()}: after onMeasure")
    }
}
