package com.mingz.share.ui

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.mingz.share.R
import com.mingz.share.databinding.LayoutShowTextBinding
import com.mingz.share.setPadding

/**
 * 用于展示或编辑文本.
 *
 * 状态列表：
 * - 不可用（不响应点击事件、不可编辑）: enable=false（editable将恒为false）
 * - 仅可点击（响应点击事件、不可编辑）: enable=true、editable=false
 * - 仅可编辑（不响应点击事件、可编辑）: enable=true、editable=true
 *
 * 状态转换：
 * - 不可用 <=> 仅可点击 : 通过[toggleStatus]转换（仅当editable=false）
 */
class ShowText(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val binding: LayoutShowTextBinding
    // “仅可编辑”状态时以下属性不为空
    private val areaRect: Rect? // 记录该组件在父组件中的位置
    // “仅可点击”状态时以下属性不为空
    private var isClick = false // 是否改为处理点击事件
    private var clickLocation: PointF? // 记录DOWN时的坐标

    /**
     * 展示或编辑的文本内容.
     */
    var content: String
        set(content) { binding.content.setText(content) }
        get() = binding.content.text.toString()

    init {
        binding = LayoutShowTextBinding.inflate(LayoutInflater.from(context), this)
        // 设置根布局参数
        setPadding(10.0f, 5.0f)
        setBackgroundResource(R.drawable.bg_fill_when_enable)
        orientation = VERTICAL
        // 读取可选参数值
        val hint: String?
        val enable: Boolean
        val editable: Boolean // 是否可编辑
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowText)
        try {
            typedArray.getString(R.styleable.ShowText_title)?.let { binding.title.text = it }
            typedArray.getString(R.styleable.ShowText_android_text)?.let { binding.content.setText(it) }
            hint = typedArray.getString(R.styleable.ShowText_android_hint)
            enable = typedArray.getBoolean(R.styleable.ShowText_android_enabled, true)
            editable = typedArray.getBoolean(R.styleable.ShowText_editable, false)
        } finally {
            typedArray.recycle()
        }
        isEnabled = enable
        // 若可编辑，areaRect将赋非空值，否则赋为空值
        if (enable) {
                if (editable) { // 组件初始化为“仅可编辑”状态
                    if (hint != null) {
                        binding.content.hint = hint
                    }
                    // 当失去焦点时收起软键盘
                    binding.content.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(binding.content.windowToken, 0)
                        }
                    }
                    areaRect = Rect()
                    clickLocation = null
                } else { // 组件初始化为“仅可点击”状态
                    notEditable() // 不可编辑
                    areaRect = null
                    clickLocation = PointF(0.0f, 0.0f)
                }
            } else { // 组件初始化为“不可用”状态
                notEditable() // 不可编辑
                areaRect = null
                clickLocation = null
            }
    }

    // 设置为不可编辑
    private fun notEditable() {
        binding.content.inputType = InputType.TYPE_NULL
        // 当获取焦点时清除焦点，避免在软键盘展开的情况下获取焦点而被编辑
        binding.content.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearFocus()
        }
    }

    /**
     * 当组件处于“仅可编辑”状态时，调用使得触摸该组件外的区域时清除焦点，否则不做任何事.
     *
     * 只能在该组件的父组件复写[dispatchTouchEvent]分配[MotionEvent.ACTION_DOWN]时调用.
     * @param x 通过[MotionEvent.getX]获得
     * @param y 通过[MotionEvent.getY]获得
     */
    fun clearFocusWhenParentDispatchDown(x: Float, y: Float) {
        if (areaRect != null && hasFocus()) {
            getGlobalVisibleRect(areaRect) // 获取该组件在父组件中的矩形区域
            if (!areaRect.contains(x.toInt(), y.toInt())) { // 若DOWN时的点不在该组件区域内，则清除焦点
                clearFocus()
            }
        }
    }

    /**
     * 用于转换该组件“仅可点击”或“不可用”的状态.
     *
     * 在“仅可编辑”状态时调用无效.
     */
    fun toggleStatus() {
        if (isEditable()) return
        val enable = if (clickLocation == null) { // 切换为“仅可点击”状态
            clickLocation = PointF(0.0f, 0.0f)
            true
        } else { // 切换为“不可用”状态
            clickLocation = null
            false
        }
        isEnabled = enable
    }

    /**
     * 当该组件处于“仅可编辑”状态时，调用无效.
     */
    override fun setOnClickListener(l: OnClickListener?) {
        if (isEditable()) return
        super.setOnClickListener(l)
    }

    // 返回组件是否处于“仅可编辑”状态
    private fun isEditable() = areaRect != null

    // 若clickLocation不为空，将会在符合条件时调用点击事件
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (clickLocation != null) { // 组件处于“仅可点击”状态
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isClick = true
                    clickLocation!!.set(ev.x, ev.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isClick) {
                        isClick = clickLocation!!.equals(ev.x, ev.y)
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