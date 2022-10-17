package com.mingz.share.ui

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.mingz.share.R
import com.mingz.share.databinding.LayoutShowTextBinding

class ShowText(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val binding: LayoutShowTextBinding
    // 该组件可用且可编辑时
    private val areaRect: Rect? // 记录该组件在父组件中的位置
    // 该组件可用但不可编辑时
    private var isClick = false // 是否改为处理点击事件
    private val clickLocation: PointF? // 记录DOWN时的坐标

    /**
     * 展示或编辑的文本内容.
     */
    var text: String
        set(content) { binding.content.setText(content) }
        get() = binding.content.text.toString()

    init {
        binding = LayoutShowTextBinding.inflate(LayoutInflater.from(context), this)
        val dm = context.resources.displayMetrics
        // 设置根布局参数
        val horizontalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, dm).toInt()
        val verticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f, dm).toInt()
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        setBackgroundResource(R.drawable.bg_fill_when_enable)
        orientation = VERTICAL
        // 读取可选参数值
        val enable: Boolean
        val editable: Boolean // 是否可编辑
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowText)
        try {
            typedArray.getString(R.styleable.ShowText_title)?.let { binding.title.text = it }
            enable = typedArray.getBoolean(R.styleable.ShowText_android_enabled, true)
            editable = typedArray.getBoolean(R.styleable.ShowText_editable, false)
            isEnabled = enable
            if (enable) {
                if (editable) { // 可编辑，保持默认（多行自适应高度文本）；不变更触摸事件
                    // 当失去焦点时收起软键盘
                    binding.content.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(binding.content.windowToken, 0)
                        }
                    }
                    areaRect = Rect()
                    clickLocation = null
                } else { // 不可编辑，设为单行滑动文本；需要变更触摸事件
                    notEditable()
                    areaRect = null
                    clickLocation = PointF(0.0f, 0.0f)
                }
            } else { // 不可编辑，设为单行滑动文本；不变更触摸事件
                notEditable()
                areaRect = null
                clickLocation = null
            }
        } finally {
            typedArray.recycle()
        }
    }

    // 设置为不可编辑
    private fun notEditable() {
        binding.content.inputType = InputType.TYPE_NULL
        // 当获取焦点时清除焦点，避免在软键盘展开的情况下获取焦点而被编辑
        binding.content.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.content.clearFocus()
        }
    }

    /**
     * 当组件可用且可编辑时，调用使得触摸该组件外的区域时清除焦点.
     *
     * 只能在该组件的父组件执行[dispatchTouchEvent]分配[MotionEvent.ACTION_DOWN]时调用.
     */
    fun clearFocusWhenParentDispatchDown(x: Float, y: Float) {
        if (areaRect != null && hasFocus()) {
            getGlobalVisibleRect(areaRect) // 获取该组件在父组件中的矩形区域
            if (!areaRect.contains(x.toInt(), y.toInt())) { // 若DOWN时的点不在该组件区域内，则清除焦点
                clearFocus()
            }
        }
    }

    // 若clickLocation不为空，将会在符合条件时调用点击事件
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (clickLocation != null) {
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
                        performClick() // 改为处理点击事件
                        return true // 终止事件传递
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}