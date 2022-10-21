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
 * - 不可用（不响应点击事件、不可编辑）: enable=false
 * - 可点击（响应点击事件、不可编辑）: enable=true、editable=false
 * - 可编辑（不响应点击事件、可编辑）: enable=true、editable=true
 *
 * 状态转换：
 * - 不可用 <=> 可点击 : 通过
 * - 不可用 <=> 可编辑 : 通过
 */
class ShowText(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val binding: LayoutShowTextBinding
    private val editable: Boolean // 是否可编辑，否则为可点击
    // “可编辑”状态参数
    private val areaRect: Rect? // 记录该组件在父组件中的位置，当editable属性为false时为空
    // “可点击”状态参数
    private var isClick = false // 是否改为处理点击事件
    private val clickLocation: PointF? // 记录DOWN时的坐标，当editable属性为true时为空

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
        val enable: Boolean // 是否可用
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
        isEnabled = enable // 设置背景状态
        if (editable) { // “可编辑”或“不可用”
            areaRect = Rect()
            clickLocation = null
            if (hint != null) { // 设置输入提示文本
                binding.content.hint = hint
            }
            // 当失去焦点时收起软键盘
            binding.content.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.content.windowToken, 0)
                }
            }
        } else { // “可点击”或“不可用”
            areaRect = null
            clickLocation = PointF(0.0f, 0.0f)
            binding.content.inputType = InputType.TYPE_NULL // 设置不可输入
            // 当获取焦点时清除焦点，避免在软键盘展开的情况下获取焦点而被编辑
            binding.content.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) clearFocus()
            }
        }
    }

    /**
     * 当组件处于“可编辑”状态时，调用使得触摸该组件外的区域时清除焦点.
     *
     * 应该在该组件的父组件复写[dispatchTouchEvent]分配[MotionEvent.ACTION_DOWN]时调用.
     * @param x 通过[MotionEvent.getX]获得
     * @param y 通过[MotionEvent.getY]获得
     */
    fun clearFocusWhenParentDispatchDown(x: Float, y: Float) {
        if (hasFocus() && isEnabled && editable) {
            getGlobalVisibleRect(areaRect) // 获取该组件在父组件中的矩形区域
            if (!areaRect!!.contains(x.toInt(), y.toInt())) { // 若DOWN时的点不在该组件区域内，则清除焦点
                clearFocus()
            }
        }
    }

    /**
     * 使组件在“不可用”状态和“可编辑”或“可点击”状态之间转换.
     */
    fun setEnableStatus(enable: Boolean) {
        isEnabled = enable
    }

    /**
     * 若组件是可编辑的([editable]值为true)，调用无效.
     */
    override fun setOnClickListener(l: OnClickListener?) {
        // 当组件是可编辑的，不应响应点击事件
        if (editable) return
        super.setOnClickListener(l)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (editable) {
            if (!isEnabled) { // 组件是可编辑的，但当前不可用
                return false // 忽略所有触摸事件，传递给父组件
            } // else -> 子组件未消费事件可能导致事件传递到该组件（如点击）
        } else { // 组件是可点击的
            // 针对点击事件，根据是否可用决定响应点击或是忽略后续触摸事件
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
                        val clickable = isEnabled
                        if (clickable) { // 响应点击事件，消费事件后终止事件传递
                            performClick()
                        } // else -> 不响应点击，触摸事件传递给父组件
                        return clickable
                    }
                }
                else -> isClick = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}