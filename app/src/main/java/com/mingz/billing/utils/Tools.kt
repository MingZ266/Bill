package com.mingz.billing.utils

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.mingz.billing.R
import com.mingz.billing.ui.DateTimePicker
import com.mingz.billing.ui.ShowDateTime

class Tools {
    companion object {
        fun chooseDateTime(context: Context, showDateTime: ShowDateTime) {
            showDateTime.setOnClickListener { _ ->
                val dialogView = View.inflate(context, R.layout.dialog_date_time_picker, null)
                val dialog = AlertDialog.Builder(context).create()
                dialog.setCanceledOnTouchOutside(false)
                dialog.show() // 必须先调用show()才能设置参数
                dialog.window?.let { window ->
                    window.setContentView(dialogView)
                    // 必须调用以铺满空间
                    window.setBackgroundDrawable(null)
                    val params = window.attributes
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    params.gravity = Gravity.BOTTOM
                    window.attributes = params
                }
                val cancel = dialogView.findViewById<View>(R.id.cancel)
                val confirm = dialogView.findViewById<View>(R.id.confirm)
                val picker = dialogView.findViewById<DateTimePicker>(R.id.picker)
                picker.setDateTime(showDateTime.year, showDateTime.month, showDateTime.day,
                    showDateTime.hour, showDateTime.minute)
                cancel.setOnClickListener { _ -> dialog.cancel() }
                confirm.setOnClickListener { _ ->
                    showDateTime.setTime(picker.year, picker.month, picker.day,
                        picker.hour, picker.minute)
                    dialog.cancel()
                }
            }
        }
    }
}