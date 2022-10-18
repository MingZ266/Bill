package com.mingz.share

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding

/**
 * 设置水平和垂直方向内边距.
 *
 * 单位为dp.
 */
internal fun View.setPadding(horizontal: Float, vertical: Float) {
    val dm = context.resources.displayMetrics
    val horizontalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horizontal, dm).toInt()
    val verticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, vertical, dm).toInt()
    setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
}

/**
 * 对[AlertDialog]的包装，保持对[AlertDialog]的引用，使其只初始化一次.
 * @param creator 第一次调用[show]时，用于初始化相关参数
 * @param bottomDialog 是否为底部弹窗，默认为否
 */
class DialogPack<BINDING: ViewBinding> (
    private val context: Context, private val creator: Creator<BINDING>,
    private val bottomDialog: Boolean = false
) {
    /**
     * 应在调用过[show]后调用.
     */
    lateinit var binding: BINDING
        private set

    /**
     * 在调用过[show]后将不为空.
     */
    var dialog: AlertDialog? = null
        private set

    /**
     * 显示弹窗.
     *
     * 仅在第一次调用时初始化弹窗.
     */
    fun show() {
        if (dialog == null) {
            binding = creator.createBinding()
            if (bottomDialog) {
                dialog = AlertDialog.Builder(context).create()
                dialog!!.show() // 必须先调用show()再设置参数
                // 设置为底部弹窗
                dialog!!.window?.let { window ->
                    window.setContentView(binding.root)
                    window.setWindowAnimations(R.style.BottomDialogAnim)
                    // 必须调用以铺满空间
                    window.setBackgroundDrawable(null)
                    val params = window.attributes
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    params.gravity = Gravity.BOTTOM
                    window.attributes = params
                }
            } else {
                dialog = AlertDialog.Builder(context, R.style.RoundCornerDialog)
                    .setView(binding.root)
                    .create()
                dialog!!.show()
            }
            creator.initDialog(dialog!!, binding)
        } else {
            dialog!!.show()
        }
    }

    /**
     * @see AlertDialog.dismiss
     */
    fun dismiss() = dialog?.dismiss()

    interface Creator<BINDING: ViewBinding> {
        fun createBinding(): BINDING

        /**
         * 初始化[DialogPack.dialog].
         */
        fun initDialog(dialog: AlertDialog, binding: BINDING) {}
    }
}