package com.mingz.billing.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.mingz.billing.MyApplication
import com.mingz.billing.R

class TestView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val myLog = MyApplication.testLog
    private val oneDp: Float
    private val mHeight: Int
    // 描边
    private val strokePaint: Paint
    // 标志
    private val myTag: String
    private val tagPaint: Paint
    // 标志文本基线偏移量
    private val baselineOffset: Float

    // 触摸事件处理机制返回值
    var dispatchReturn: Boolean? = null
    var doReturn: Boolean? = null

    init {
        val dm = context.resources.displayMetrics
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        mHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34f, dm).toInt()
        strokePaint = Paint()
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = oneDp
        tagPaint = Paint()
        tagPaint.textAlign = Paint.Align.CENTER
        tagPaint.isAntiAlias = true
        // 标志文本尺寸
        tagPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22f, dm)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TestView)
        try {
            myTag = "View - ${typedArray.getString(R.styleable.TestView_myTag)}"
            val color = typedArray.getColor(R.styleable.TestView_color, Color.BLACK)
            strokePaint.color = color
            tagPaint.color = color
        } finally {
            typedArray.recycle()
        }
        val tagRect = Rect()
        tagPaint.getTextBounds(myTag, 0, myTag.length, tagRect)
        baselineOffset = (tagRect.bottom + tagRect.top) / 2.0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        canvas.drawColor(Color.WHITE)
        // 绘制边框
        canvas.drawRect(oneDp, oneDp, width - oneDp, height - oneDp, strokePaint)
        // 绘制标志
        val centerX = width / 2.0f
        val centerY = height / 2.0f
        canvas.drawText(myTag, centerX, centerY - baselineOffset, tagPaint)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (dispatchReturn == null) {
            myLog.d("$myTag => 分配(执行super): ${toString(ev)}")
            super.dispatchTouchEvent(ev)
        } else if (dispatchReturn!!) {
            myLog.d("$myTag => 分配(返回true): ${toString(ev)}")
            true
        } else {
            myLog.d("$myTag => 分配(返回false): ${toString(ev)}")
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (doReturn == null) {
            myLog.d("$myTag => 消费(执行super): ${toString(event)}")
            super.onTouchEvent(event)
        } else if (doReturn!!) {
            myLog.d("$myTag => 消费(返回true): ${toString(event)}")
            true
        } else {
            myLog.d("$myTag => 消费(返回false): ${toString(event)}")
            false
        }
    }

    private fun toString(event: MotionEvent) = MotionEvent.actionToString(event.action) +
            "(${String.format("%.2f, %.2f", event.x, event.y)})"
}