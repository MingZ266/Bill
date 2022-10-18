package com.mingz.share.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import androidx.annotation.IntRange
import com.mingz.share.MyLog
import com.mingz.share.databinding.LayoutInputAmountBinding
import java.math.BigDecimal

/**
 * 输入指定精确度的数额.
 *
 * 输入前必须先调用[initParams]进行初始化.
 *
 * 各按键意图：
 * - 数值键（0~9）：输入数字
 * - 负号键（-）：若选中，则输入数额将视为负数；否则为非负数
 * - 小数点（.）：若此前为输入整数部分，则此后视为输入小数部分
 * - 清零键（CE）：将数额置零，同时将接下来的输入视为输入整数部分
 * - 退格键（DEL）：若当前为输入整数部分且整数部分不为0，将删除个位数字；若当前为输入小数部分，将上一个输入的
 *                数字置零；若小数部分已全部退格，则将退格整数部分，同时将接下来的输入视为输入整数部分
 * - 确认键（OK）：通过[inputListener]通知输入的数额
 *
 * 初始化后的第一次按键若为下列各键时的意图：
 * - 数值键（0~9）：先清零，再输入整数部分
 * - 小数点（.）：意图同上，但下一个数字输入时，将先对小数部分清零，再输入小数部分
 * - 退格键（DEL）：将小数部分最后一位数字置零，然后将接下来的输入视为输入小数部分
 */
class InputAmount(context: Context, attrs: AttributeSet? = null) : GridLayout(context, attrs) {
    private val myLog by lazy(LazyThreadSafetyMode.NONE) { MyLog(this::class) }
    private val binding: LayoutInputAmountBinding
    private var inputListener: InputListener? = null
    private var accuracy = 2 // 精确度
    private lateinit var format: String// 用于格式化数额精确度
    // 数额
    private lateinit var sign: String // 符号（若为负数，则为“-”，否则为空字符串）
    private var integer = 0 // 整数部分
    private lateinit var decimal: String // 格式化后的小数部分，也是当前数额的小数部分
    private val decimalPart = StringBuilder() // 已输入的小数部分，不一定是当前数额的小数部分
    // 状态变量
    private var firstKey = true // 是否是初始化后的第一次按键
    private var inputInteger = true // 当前是否为输入整数部分

    init {
        binding = LayoutInputAmountBinding.inflate(LayoutInflater.from(context), this)
        // 设置根布局参数
        columnCount = 4
        // 数值键（0~9）
        binding.key0.setOnClickListener { valueKeyListener(0) }
        binding.key1.setOnClickListener { valueKeyListener(1) }
        binding.key2.setOnClickListener { valueKeyListener(2) }
        binding.key3.setOnClickListener { valueKeyListener(3) }
        binding.key4.setOnClickListener { valueKeyListener(4) }
        binding.key5.setOnClickListener { valueKeyListener(5) }
        binding.key6.setOnClickListener { valueKeyListener(6) }
        binding.key7.setOnClickListener { valueKeyListener(7) }
        binding.key8.setOnClickListener { valueKeyListener(8) }
        binding.key9.setOnClickListener { valueKeyListener(9) }
        // 负号键（-）
        binding.keyNegative.setOnCheckedChangeListener { _, isChecked ->
            sign = if (isChecked) "-" else ""
            updateAmount()
        }
        // 小数点（.）
        binding.keyPoint.setOnClickListener {
            inputInteger = false // 接下来输入小数部分
            if (firstKey) {
                firstKey = false
                // decimalPart在初始化后为空，因此下一次输入等效于先对小数部分清零再输入
            }
        }
        // 退格键（DEL）
        binding.keyDEL.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                if (firstKey) { // 将小数部分最后一位置零
                    firstKey = false
                    inputInteger = false // 接下来视为输入小数部分
                    decimalPart.append(decimal) // 将初始化的数额的小数部分视为已输入的小数部分
                    delOnInputDecimal()
                } else if (inputInteger) { // 删除整数部分个位数字
                    delOnInputInteger()
                } else if (decimalPart.isEmpty()) { // 改为删除整数部分个位数字
                    inputInteger = true // 改为输入整数部分
                    delOnInputInteger()
                } else { // 将小数部分上一个输入的数字置零
                    delOnInputDecimal()
                }
                updateAmount()
            }

            private fun delOnInputInteger() {
                if (integer == 0) return // 整数部分已经是0时继续退格
                integer /= 10 // 删除整数部分个位数字
            }

            private fun delOnInputDecimal() {
                decimalPart.deleteAt(decimalPart.lastIndex)
                formatDecimal()
            }
        })
        // 清零键（CE）
        binding.keyCE.setOnClickListener {
            if (binding.keyNegative.isChecked) {
                binding.keyNegative.isChecked = false
            }
            // 清除数额与记录
            sign = ""
            integer = 0
            formatDecimal(true)
            decimalPart.clear()
            // 设置状态
            firstKey = false
            inputInteger = true
            updateAmount()
        }
        // 确认键（OK）
        binding.keyOK.setOnClickListener {
            val prefix = if (integer == 0 && decimal.toInt() == 0) "" else sign
            inputListener?.onOk("$prefix$integer.$decimal")
        }
    }

    // 数值键监听
    private fun valueKeyListener(value: Int) {
        if (firstKey) {
            firstKey = false
            // 清零
            formatDecimal(true)
            decimalPart.clear()
            // 输入整数部分
            integer = value
        } else {
            if (inputInteger) { // 输入整数部分
                if ((value == 0 && integer == 0)) { // 在整数部分为0时继续输入0
                    return
                }
                val temp: Long = integer.toLong() * 10 + value
                if (temp > Int.MAX_VALUE) { // 超出整数部分最大值
                    return
                }
                integer = temp.toInt()
            } else { // 输入小数部分
                if (decimalPart.length >= accuracy) { // 小数部分长度已达精确度上限
                    return
                }
                decimalPart.append(value) // 记录输入的小数部分
                formatDecimal()
            }
        }
        updateAmount()
    }

    // 格式化小数部分
    private fun formatDecimal(setZero: Boolean = false) {
        decimal = if (setZero) {
            String.format(format, 0.0).substring(2)
        } else {
            String.format(format, BigDecimal("0.$decimalPart")).substring(2)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAmount() {
        binding.amount.text = "$sign$integer.$decimal"
    }

    /**
     * 设置初始化参数.
     * @param initValue 初始数额，默认为"0"
     * @param accuracy 精确度，默认为2
     * @param allowNeg 是否允许输入负值，默认为false；若[initValue]为负数，该值将视为true
     */
    fun initParams(initValue: String = "0", @IntRange(from = 1) accuracy: Int = 2,
                   allowNeg: Boolean = false) {
        // 重置状态变量
        firstKey = true
        inputInteger = true
        // 设置精确度及格式化字符串
        this.accuracy = accuracy
        format = "%.${accuracy}f"
        // 分割初始数额
        val amount = try {
            String.format(format, BigDecimal(initValue))
        } catch (e: Exception) { // 不是合法的数值
            myLog.i("初始值不是合法的数值: $initValue")
            String.format(format, 0.0)
        }
        val index = amount.indexOf('.')
        decimal = amount.substring(index + 1) // 记录初始化数额的小数部分
        decimalPart.clear() // 清空可能记录的小数部分
        if (amount[0] == '-') { // 负数
            integer = amount.substring(1, index).toInt() // 记录初始化数额的整数部分
            sign = "-" // 记录初始化数额的符号部分
            binding.keyNegative.isEnabled = true // 启用负号键
            binding.keyNegative.isChecked = true // 设置负号键选中
        } else { // 非负数
            integer = amount.substring(0, index).toInt() // 记录初始化数额的整数部分
            sign = "" // 记录初始化数额的符号部分
            binding.keyNegative.isEnabled = allowNeg // 设置是否启用负号键
            if (allowNeg) {
                binding.keyNegative.isChecked = false // 设置负号键不选中
            }
        }
        binding.amount.text = amount // 设置显示的数额
    }

    /**
     * 设置按键监听.
     */
    fun setInputListener(listener: InputListener) {
        inputListener = listener
    }

    interface InputListener {
        /**
         * 当“Ok”键按下时.
         * @param value 输入的数额
         */
        fun onOk(value: String)
    }
}