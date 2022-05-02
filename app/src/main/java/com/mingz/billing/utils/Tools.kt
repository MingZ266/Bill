package com.mingz.billing.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.view.*
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.appcompat.app.AlertDialog
import com.mingz.billing.databinding.DialogInputAmountOfMoneyBinding
import com.mingz.billing.databinding.DialogSelectItemBinding
import com.mingz.billing.databinding.DialogSelectSubjectBinding
import com.mingz.billing.databinding.ItemDialogContentListBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.text.StringBuilder

class Tools {
    companion object {
        private val myLog by lazy { MyLog("Tools") }
        //******弹窗******
        @JvmStatic
        fun setAsBottomPopupAndShow(dialog: AlertDialog, content: View) {
            dialog.show() // 必须先调用show()再设置参数
            dialog.window?.let { window ->
                window.setContentView(content)
                // 必须调用以铺满空间
                window.setBackgroundDrawable(null)
                val params = window.attributes
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                params.gravity = Gravity.BOTTOM
                window.attributes = params
            }
        }

        @JvmStatic
        fun showBottomPopup(context: Context, content: View): AlertDialog {
            val dialog = AlertDialog.Builder(context).create()
            setAsBottomPopupAndShow(dialog, content)
            return dialog
        }

        @SuppressLint("SetTextI18n")
        @JvmStatic
        fun inputAmountOfMoney(context: Context, title: String = "金额",
                               initValue: String = "0", @IntRange(from = 1) accuracy: Int = 2,
                               allowNeg: Boolean = false, onResult: (String) -> Unit) {
            val binding = DialogInputAmountOfMoneyBinding.inflate(LayoutInflater.from(context))
            val dialog = showBottomPopup(context, binding.root)
            dialog.setCanceledOnTouchOutside(false)
            // 初始化参数
            val format = "%.${accuracy}f"
            val amount = try {
                String.format(format, BigDecimal(initValue))
            } catch (e: Exception) {
                String.format(format, 0.0)
            } // 至少形如：0.0 或 -0.1
            val index = amount.indexOf('.')
            var decimal = amount.substring(index + 1)
            var integer: Int
            var signPrefix: String
            if (amount[0] == '-') {
                integer = amount.substring(1, index).toInt()
                signPrefix = "-"
                binding.keyNegative.isChecked = true
            } else {
                integer = amount.substring(0, index).toInt()
                signPrefix = ""
            }
            // 初始化视图
            binding.title.text = title
            binding.amount.text = amount
            binding.keyNegative.isEnabled = allowNeg
            // 设置监听
            binding.putAway.setOnClickListener { dialog.cancel() }
            binding.keyOK.setOnClickListener {
                onResult(binding.amount.text.toString())
                dialog.cancel()
            }
            var hasNotInput = true
            var inputInteger = true
            var append = false
            val decimalPart = StringBuilder()
            val valueKeyListener = input@{ value: Char ->
                hasNotInput = false
                val intValue = value.code - '0'.code
                var prefix = signPrefix
                if (inputInteger) {
                    if (append) {
                        // 整型最大值: 2147483647
                        if ((intValue == 0 && integer == 0) || integer >= 214748364) {
                            return@input
                        }
                        integer = integer * 10 + intValue
                    } else {
                        append = true
                        integer = intValue
                        decimal = String.format(format, 0.0).substring(2)
                        if (integer == 0) {
                            prefix = ""
                        }
                    }
                } else {
                    if (append) {
                        if (decimalPart.length >= accuracy) {
                            return@input
                        }
                        decimalPart.append(intValue)
                        decimal = String.format(format, BigDecimal("0.$decimalPart"))
                            .substring(2)
                        if (integer == 0 && decimal.toInt() == 0) {
                            prefix = ""
                        }
                    } else {
                        append = true
                        decimalPart.append(intValue) // decimalPart此时即: intValue
                        decimal = String.format(format, BigDecimal("0.$intValue"))
                            .substring(2)
                        if (integer == 0 && intValue == 0) {
                            prefix = ""
                        }
                    }
                }
                binding.amount.text = "$prefix$integer.$decimal"
            }
            binding.key1.setOnClickListener { valueKeyListener('1') }
            binding.key2.setOnClickListener { valueKeyListener('2') }
            binding.key3.setOnClickListener { valueKeyListener('3') }
            binding.key4.setOnClickListener { valueKeyListener('4') }
            binding.key5.setOnClickListener { valueKeyListener('5') }
            binding.key6.setOnClickListener { valueKeyListener('6') }
            binding.key7.setOnClickListener { valueKeyListener('7') }
            binding.key8.setOnClickListener { valueKeyListener('8') }
            binding.key9.setOnClickListener { valueKeyListener('9') }
            binding.key0.setOnClickListener { valueKeyListener('0') }
            binding.keyPoint.setOnClickListener {
                if (inputInteger) {
                    inputInteger = false
                    append = false
                }
            }
            binding.keyNegative.setOnCheckedChangeListener { _, isChecked ->
                signPrefix = if (isChecked) "-" else ""
                if (integer != 0 || decimal.toInt() != 0) {
                    binding.amount.text = "$signPrefix$integer.$decimal"
                }
            }
            binding.keyCE.setOnClickListener {
                inputInteger = true
                append = false
                decimalPart.clear()
                integer = 0
                decimal = String.format(format, 0.0).substring(2)
                binding.amount.text = "0.$decimal"
            }
            binding.keyDEL.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    if (hasNotInput) {
                        inputInteger = true
                        integer = 0
                        decimal = String.format(format, 0.0).substring(2)
                        binding.amount.text = "0.$decimal"
                        return
                    }
                    onDelete()
                }

                private fun onDelete() {
                    var prefix = signPrefix
                    if (inputInteger) {
                        if (integer == 0) {
                            return
                        }
                        integer /= 10
                        if (integer == 0) {
                            prefix = ""
                        }
                    } else {
                        if (decimalPart.isEmpty()) {
                            inputInteger = true
                            onDelete()
                            return
                        } else {
                            decimalPart.deleteAt(decimalPart.lastIndex)
                            decimal = String.format(format, BigDecimal("0.$decimalPart"))
                                .substring(2)
                            if (integer == 0 && decimal.toInt() == 0) {
                                prefix = ""
                            }
                        }
                    }
                    binding.amount.text = "$prefix$integer.$decimal"
                }
            })
        }

        @JvmStatic
        fun showSelectPopup(context: Context, title: String, contentList: StringList,
                            checkedId: Int, onResult: (StringWithId) -> Unit,
                            onEdit: (() -> Unit)? = null) {
            val binding = DialogSelectItemBinding.inflate(LayoutInflater.from(context))
            val dialog = showBottomPopup(context, binding.root)
            dialog.setCanceledOnTouchOutside(false)
            // 初始化视图
            binding.title.text = title
            binding.contentList.adapter = SelectContentAdapter(context, contentList, checkedId)
            if (onEdit != null) {
                binding.edit.visibility = View.VISIBLE
            }
            // 设置监听
            binding.putAway.setOnClickListener { dialog.cancel() }
            binding.contentList.setOnItemClickListener { _, _, position, _ ->
                contentList.floatToTop(position)
                onResult(contentList[0])
                dialog.cancel()
            }
            binding.edit.setOnClickListener {
                dialog.cancel()
                if (onEdit != null) {
                    onEdit()
                }
            }
        }

        @JvmStatic
        fun showSelectSubject(context: Context, title: String, subject: SubjectArray,
                              checkedId: Int, onResult: (StringWithId) -> Unit) {
            val binding = DialogSelectSubjectBinding.inflate(LayoutInflater.from(context))
            val dialog = showBottomPopup(context, binding.root)
            dialog.setCanceledOnTouchOutside(false)
            // 初始化视图
            binding.title.text = title
            binding.subjectList.setDataAndExpandAll(subject.toArray())
            binding.subjectList.setAllowExpandFold(false)
            binding.subjectList.post {
                binding.subjectList.setItemChecked(DataSource.checkedPosition, true)
                binding.subjectList.setSelection(DataSource.checkedPosition)
            }
            // 设置监听
            binding.putAway.setOnClickListener { dialog.cancel() }
            var select: StringWithId? = null
            var checkedPosition = -1
            binding.subjectList.setOnItemClickListener { data, position ->
                if (data is SubjectLvOne) {
                    select = data.data
                    checkedPosition = position
                } else if (data is SubjectLvTwo) {
                    select = data.data
                    checkedPosition = position
                }
            }
            binding.selectIt.setOnClickListener {
                if (select == null) {
                    return@setOnClickListener
                }
                DataSource.checkedPosition = checkedPosition
                onResult(select!!)
                dialog.cancel()
            }
        }
        //******安全******
        private val digest = MessageDigest.getInstance("MD5")

        @JvmStatic
        fun md5AsBytes(content: ByteArray): ByteArray = digest.digest(content)

        @JvmStatic
        fun md5(content: String): String = md5(content.toByteArray(StandardCharsets.UTF_8))

        @JvmStatic
        fun md5(content: ByteArray) = bytesToBase64(digest.digest(content))

        @JvmStatic
        fun bytesToBase64(data: ByteArray): String {
            return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING)
        }
        //******文件******
        @JvmStatic
        fun saveFile(file: File, data: ByteArray, append: Boolean = false) {
            try {
                FileOutputStream(file, append).use { fos ->
                    fos.write(data)
                }
            } catch (e: Exception) {
                myLog.i("文件保存失败", e)
            }
        }

        @JvmStatic
        fun saveFile(file: File, data: String, append: Boolean = false) =
            saveFile(file, data.toByteArray(StandardCharsets.UTF_8), append)

        @JvmStatic
        fun readFile(file: File): ByteArray? {
            return try {
                FileInputStream(file).use { fis ->
                    fis.readBytes()
                }
            } catch (e: Exception) {
                myLog.i("文件读取失败", e)
                null
            }
        }

        fun readFileAsString(file: File): String? = readFile(file)?.let {
            String(it, StandardCharsets.UTF_8)
        }

        //******其它******
        @JvmStatic
        fun showToast(context: Context, msg: String) =
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

        @JvmStatic
        fun showToastOnUiThread(context: Context, msg: String) {
            Handler(context.mainLooper).post {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class SelectContentAdapter(
        private val context: Context,
        private val contentList: StringList,
        private val checkedId: Int
    ) : BaseAdapter() {
        override fun getCount() = contentList.size

        override fun getItem(position: Int) = null

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val binding: ItemDialogContentListBinding
            if (convertView == null) {
                binding = ItemDialogContentListBinding.inflate(LayoutInflater.from(context))
                view = binding.root
                view.tag = binding
            } else {
                binding = convertView.tag as ItemDialogContentListBinding
                view = convertView
            }
            val data = contentList[position]
            binding.content.text = data.content
            binding.content.isChecked = data.id == checkedId
            return view
        }
    }
}