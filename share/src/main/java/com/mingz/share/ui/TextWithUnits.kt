package com.mingz.share.ui

import android.content.Context
import android.graphics.PointF
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.mingz.share.R
import com.mingz.share.databinding.LayoutTextWithUnitsBinding
import com.mingz.share.setPadding

/**
 * 用于显示带单位的数额.
 *
 * 状态列表：
 * - 不可用（不响应点击事件）: enable=false
 * - 可用（响应点击事件）: enable=true
 *
 * 状态转换：
 * - 不可用 <=> 可用 : 通过[setEnabled]转换
 */
class TextWithUnits(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {
    private val binding: LayoutTextWithUnitsBinding
    private var isClick = false // 是否改为处理点击事件
    private val clickLocation = PointF(0.0f, 0.0f) // 记录DOWN时的坐标

    init {
        binding = LayoutTextWithUnitsBinding.inflate(LayoutInflater.from(context), this)
        // 设置根布局参数
        setPadding(10.0f, 5.0f)
        setBackgroundResource(R.drawable.bg_fill_when_enable)
        // 读取可选参数
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextWithUnits)
        try {
            isEnabled = typedArray.getBoolean(R.styleable.TextWithUnits_android_enabled, true)
            typedArray.getString(R.styleable.TextWithUnits_title)?.let { binding.title.text = it }
            typedArray.getString(R.styleable.TextWithUnits_units)?.let { binding.units.text = it }
            typedArray.getString(R.styleable.TextWithUnits_android_text)?.let { binding.amount.setText(it) }
        } finally {
            typedArray.recycle()
        }
        // 不可编辑
        binding.amount.inputType = InputType.TYPE_NULL
        // 当获取焦点时清除焦点，避免在软键盘展开的情况下获取焦点而被编辑
        binding.amount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearFocus()
        }
    }

    /**
     * 设置数额.
     */
    fun setAmount(amount: String) {
        binding.amount.setText(amount)
    }

    /**
     * 设置单位.
     */
    fun setUnits(units: String) {
        binding.units.text = units
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isClick = true
                clickLocation.set(ev.x, ev.y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (isClick) {
                    isClick = clickLocation.equals(ev.x, ev.y)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isClick) {
                    if (isEnabled) {
                        performClick() // 改为处理点击事件
                    }
                    return true // 终止事件传递
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}