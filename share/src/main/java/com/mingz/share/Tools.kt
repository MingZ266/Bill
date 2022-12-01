package com.mingz.share

import android.content.Context
import android.os.Handler
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.mingz.share.databinding.DialogLoadingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 获取存储在files目录下的[File]对象.
 * @param atInternal 是否从内部存储目录下获取，若值为false但获取外部存储目录为空时，将等同于true
 * @param childPath 在files目录下的路径，其中包含的目录将会被创建（可能创建失败），但不会创建文件
 */
fun quoteFile(context: Context, atInternal: Boolean, childPath: String): File {
    val dir = if (atInternal) context.filesDir else {
        context.getExternalFilesDir("") ?: context.filesDir
    }
    return File(dir, childPath).apply { parentFile?.mkdirs() }
}

/**
 * 读取[file]内容.
 */
suspend fun readFile(file: File) = withContext(Dispatchers.IO) {
    FileInputStream(file).use { it.readBytes() }
}

/**
 * 将[data]写入[file]，如果[file]不存在将创建.
 * @return [file]是否存在或是是否创建成功
 */
suspend fun saveFile(file: File, data: ByteArray) = withContext(Dispatchers.IO) {
    (file.exists() || file.createNewFile()).onTrue {
        FileOutputStream(file).use { it.write(data) }
    }
}

/**
 * 执行并返回[action]的结果，若发生异常则捕获并返回[onException]的结果.
 */
inline fun <T> catchException(action: () -> T, onException: (e: Exception) -> T) = try {
    action()
} catch (e: Exception) {
    onException(e)
}

/**
 * 执行[action]，若发生异常则捕获并由[myLog]打印.
 */
inline fun catchException(myLog: MyLog? = null, action: () -> Unit) = catchException(action) {
    myLog?.e(it, true)
}

/**
 * 当此值为true时调用[action]，最后返回此值.
 */
inline fun Boolean.onTrue(action: () -> Unit): Boolean {
    if (this) action()
    return this
}

/**
 * 当此值为false时调用[action]，最后返回此值.
 */
inline fun Boolean.onFalse(action: () -> Unit): Boolean {
    if (!this) action()
    return this
}

/**
 * 通过[Toast]显示[message].
 */
fun Context.showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

/**
 * 在UI线程上通过[Toast]显示[message].
 */
fun Context.showToastOnUi(message: String) {
    Handler(mainLooper).post { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
}

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
open class DialogPack<BINDING: ViewBinding> (
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

/**
 * 加载中弹窗.
 */
class Loading(context: Context) : DialogPack<DialogLoadingBinding>(context,
    object : Creator<DialogLoadingBinding> {
        override fun createBinding() = DialogLoadingBinding.inflate(LayoutInflater.from(context))

        override fun initDialog(dialog: AlertDialog, binding: DialogLoadingBinding) {
            dialog.setCanceledOnTouchOutside(false) // 禁用触摸非弹窗区域关闭弹窗
            dialog.setCancelable(false) // 禁用返回键关闭弹窗
            // 设置动画
            binding.icon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.loading))
        }
    }
) {
    /**
     * 将[DialogLoadingBinding.text]设为[message]以显示.
     */
    fun show(message: String) {
        show()
        binding.text.text = message
    }
}