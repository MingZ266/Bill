package com.mingz.billing.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mingz.billing.R

class ShowText(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {
    private val content: EditText

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_show_text, this)
        content = findViewById(R.id.content)
        val root = findViewById<View>(R.id.root)
        val title = findViewById<TextView>(R.id.title)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowText)
        try {
            root.isEnabled = typedArray.getBoolean(R.styleable.ShowText_android_enabled, true)
            var titleText = typedArray.getText(R.styleable.ShowText_title)
            if (titleText == null) {
                titleText = "null"
            }
            title.text = titleText
            val editable = typedArray.getBoolean(R.styleable.ShowText_editable, false)
            content.isFocusable = editable
            content.isFocusableInTouchMode = editable
            if (editable) {
                content.hint = typedArray.getText(R.styleable.ShowText_android_hint)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    content.textCursorDrawable = ContextCompat.getDrawable(context, R.drawable.cursor_color)
                }
                // 回车时收起键盘，清除焦点
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                content.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                        inputMethodManager.hideSoftInputFromWindow(content.windowToken, 0)
                        content.clearFocus()
                        return@setOnKeyListener true
                    }
                    false
                }
            } else {
                // 禁止长按以插入文本（如粘贴）
                content.customInsertionActionModeCallback = object : ActionMode.Callback {
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

    fun setContent(content: String) {
        this.content.setText(content)
    }
}