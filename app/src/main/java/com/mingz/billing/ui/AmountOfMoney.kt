package com.mingz.billing.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mingz.billing.R
import com.mingz.billing.databinding.LayoutAmountOfMoneyBinding
import com.mingz.billing.utils.DataSource
import com.mingz.billing.utils.StringWithId
import com.mingz.billing.utils.Tools
import java.math.BigDecimal

class AmountOfMoney(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {
    private val binding: LayoutAmountOfMoneyBinding
    private var theType: StringWithId? = null

    init {
        binding = LayoutAmountOfMoneyBinding.inflate(LayoutInflater.from(context),
            this, true)
        if (DataSource.INSTANCE.typeList.isNotEmpty()) {
            theType = DataSource.INSTANCE.typeList[0]
        }
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AmountOfMoney)
        try {
            binding.root.isEnabled = typedArray.getBoolean(
                R.styleable.AmountOfMoney_android_enabled, true)
            var titleText = typedArray.getText(R.styleable.AmountOfMoney_title)
            if (titleText == null) {
                titleText = "金额"
            }
            binding.title.text = titleText
            binding.type.text = if (theType != null) theType!!.content else "null"
            val showType = typedArray.getBoolean(R.styleable.AmountOfMoney_showType, true)
            binding.type.visibility = if (showType) View.VISIBLE else View.GONE
            binding.type.setOnClickListener {
                Tools.showSelectPopup(context, titleText.toString(), DataSource.INSTANCE.typeList,
                    if (theType != null) theType!!.id else -1, {
                    theType = it
                    binding.type.text = it.content
                }) {
                    // TODO: 修改货币类型
                    Tools.showToast(context, "修改货币类型")
                }
            }
        } finally {
            typedArray.recycle()
        }
    }

    fun getTitle() = binding.title.text.toString()

    fun getAmount() = binding.amount.text.toString()

    fun getType() = binding.type.text.toString()

    fun setAmount(amount: String) {
        binding.amount.text = amount
    }

    fun setAmount(amount: BigDecimal) {
        binding.amount.text = amount.toPlainString()
    }
}