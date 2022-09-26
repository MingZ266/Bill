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
import android.view.ViewGroup
import com.mingz.billing.MyApplication
import com.mingz.billing.R

class TestViewGroup(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {
    private val myLog = MyApplication.testLog
    private val oneDp: Float
    // 子组件间距
    private val spacing: Int
    // 描边
    private val strokePaint: Paint
    // 标志
    private val myTag: String
    // 标志区域高度
    private val tagAreaHeight: Float
    private val tagPaint: Paint
    // 标志文本基线偏移量
    private val baselineOffset: Float
    // 分隔块
    private val splitPaint: Paint

    // 触摸事件处理机制返回值
    var dispatchReturn: Boolean? = null
    var interceptReturn: Boolean? = null
    var doReturn: Boolean? = null

    init {
        val dm = context.resources.displayMetrics
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        tagAreaHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, dm)
        spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, dm).toInt()
        strokePaint = Paint()
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = oneDp
        tagPaint = Paint()
        tagPaint.textAlign = Paint.Align.CENTER
        tagPaint.isAntiAlias = true
        // 标志文本尺寸
        tagPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22f, dm)
        splitPaint = Paint()
        splitPaint.style = Paint.Style.FILL
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TestViewGroup)
        try {
            myTag = "ViewGroup - ${typedArray.getString(R.styleable.TestViewGroup_myTagGroup)}"
            val color = typedArray.getColor(R.styleable.TestViewGroup_colorGroup, Color.BLACK)
            strokePaint.color = color
            tagPaint.color = color
            splitPaint.color = color
        } finally {
            typedArray.recycle()
        }
        val tagRect = Rect()
        tagPaint.getTextBounds(myTag, 0, myTag.length, tagRect)
        baselineOffset = (tagRect.bottom + tagRect.top) / 2.0f
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = width
        val count = childCount
        var top = tagAreaHeight.toInt() + spacing * 2
        for (i in 0 until count) {
            val child = getChildAt(i)
            val bottom = top + child.measuredHeight
            child.layout(spacing, top, width - spacing, bottom)
            top = bottom + spacing
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        canvas.drawColor(Color.WHITE)
        // 绘制边框
        canvas.drawRect(oneDp, oneDp, width - oneDp, height - oneDp, strokePaint)
        // 绘制标志
        val centerX = width / 2.0f
        val centerY = tagAreaHeight / 2.0f + oneDp * 2.0f
        canvas.drawText(myTag, centerX, centerY - baselineOffset, tagPaint)
        // 绘制分隔块
        val top = tagAreaHeight + oneDp * 2.0f
        canvas.drawRect(oneDp, top, width - oneDp, top + spacing, splitPaint)
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

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (interceptReturn == null) {
            myLog.d("$myTag => 中断(执行super): ${toString(ev)}")
            super.onInterceptTouchEvent(ev)
        } else if (interceptReturn!!) {
            myLog.d("$myTag => 中断(返回true): ${toString(ev)}")
            true
        } else {
            myLog.d("$myTag => 中断(返回false): ${toString(ev)}")
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