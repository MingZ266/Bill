package com.mingz.security.verify

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment

abstract class SafetyVerifyFragment : Fragment() {
    /**
     * 用于查找的TAG.
     */
    internal abstract val mTag: String

    companion object {
        private var callback: VerifyCallback? = null

        /**
         * 设置安全项验证结果回调.
         */
        @JvmStatic
        fun setVerifyCallback(callback: VerifyCallback) {
            SafetyVerifyFragment.callback = callback
        }
    }

    /**
     * 初始化视图及设置监听.
     */
    protected abstract fun initView()

    /**
     * 当该[SafetyVerifyFragment]隐藏后调用.
     */
    internal open fun onHide() {}

    /**
     * 当该[SafetyVerifyFragment]显示前调用.
     */
    internal open fun onShow() {}

    /**
     * 当安全项验证成功时回调.
     */
    protected fun onVerifySuccess() {
        val context = context ?: return
        callback?.onSuccess(context)
        callback = null
    }

    @CallSuper
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (!isHidden) {
            initView()
        }
    }

    /**
     * 安全项验证结果回调.
     */
    interface VerifyCallback {
        /**
         * 当安全项验证成功时.
         */
        fun onSuccess(context: Context)
    }
}