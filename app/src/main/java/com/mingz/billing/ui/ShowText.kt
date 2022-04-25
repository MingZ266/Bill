package com.mingz.billing.ui

import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.mingz.billing.R
import com.mingz.billing.databinding.LayoutShowTextBinding
import com.mingz.billing.utils.StringList
import com.mingz.billing.utils.Tools

class ShowText(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {
    private val binding: LayoutShowTextBinding
    private val editable: Boolean
    private var position = PointF(0.0f, 0.0f)

    init {
        binding = LayoutShowTextBinding.inflate(LayoutInflater.from(context),
            this, true)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowText)
        try {
            binding.root.isEnabled = typedArray.getBoolean(R.styleable.ShowText_android_enabled, true)
            var titleText = typedArray.getText(R.styleable.ShowText_title)
            if (titleText == null) {
                titleText = "null"
            }
            binding.title.text = titleText
            editable = typedArray.getBoolean(R.styleable.ShowText_editable, false)
            binding.content.isFocusable = editable
            binding.content.isFocusableInTouchMode = editable
            if (editable) {
                binding.content.hint = typedArray.getText(R.styleable.ShowText_android_hint)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    binding.content.textCursorDrawable = ContextCompat.getDrawable(context, R.drawable.cursor_color)
                }
                // 回车时收起键盘，清除焦点
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                binding.content.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                        inputMethodManager.hideSoftInputFromWindow(binding.content.windowToken, 0)
                        binding.content.clearFocus()
                        return@setOnKeyListener true
                    }
                    false
                }
            } else {
                // 禁止长按以插入文本（如粘贴）
                binding.content.customInsertionActionModeCallback = object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false

                    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

                    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = true

                    override fun onDestroyActionMode(mode: ActionMode?) {}
                }
            }
        } finally {
            typedArray.recycle()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!editable) {
            when (ev.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> position.set(ev.x, ev.y)
                MotionEvent.ACTION_UP -> {
                    if (position.equals(ev.x, ev.y)) {
                        // 模拟点击
                        performClick()
                        // 不再继续分发
                        return true
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun setSelectItem(contentList: StringList, onEdit: (() -> Unit)? = null) {
        setOnClickListener(object : OnClickListener {
            private var theContent: StringList.StringWithId? = null

            override fun onClick(v: View?) {
                Tools.showSelectPopup(context, binding.title.text.toString(), contentList,
                    if (theContent != null) theContent!!.id else -1, {
                    theContent = it
                    binding.content.setText(it.content)
                }, onEdit)
            }
        })
    }

    fun getTitle() = binding.title.text.toString()

    fun getContent() = binding.content.text.toString()

    fun setContent(content: String) {
        binding.content.setText(content)
    }
}