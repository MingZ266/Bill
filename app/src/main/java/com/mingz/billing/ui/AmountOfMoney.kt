package com.mingz.billing.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.mingz.billing.R
import java.math.BigDecimal

class AmountOfMoney(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {
    private val amount: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_amount_of_money, this)
        amount = findViewById(R.id.amount)
        val root = findViewById<View>(R.id.root)
        val title = findViewById<TextView>(R.id.title)
        val type = findViewById<TextView>(R.id.type)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AmountOfMoney)
        try {
            root.isEnabled = typedArray.getBoolean(R.styleable.AmountOfMoney_android_enabled, true)
            var titleText = typedArray.getText(R.styleable.AmountOfMoney_title)
            if (titleText == null) {
                titleText = "金额"
            }
            title.text = titleText
            var typeText = typedArray.getText(R.styleable.AmountOfMoney_type)
            if (typeText == null) {
                typeText = "null"
            }
            type.text = typeText
            val showType = typedArray.getBoolean(R.styleable.AmountOfMoney_showType, true)
            type.visibility = if (showType) View.VISIBLE else View.GONE
        } finally {
            typedArray.recycle()
        }
    }

    fun setAmount(amount: BigDecimal) {
        this.amount.text = String.format("%.2f", amount)
    }
}