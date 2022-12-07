package com.mingz.security.verify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mingz.security.AES
import com.mingz.security.CFG_PATTERN_BOOL
import com.mingz.security.SafeKey
import com.mingz.security.databinding.FragmentPatternBinding
import com.mingz.security.option.PatternActivity
import com.mingz.share.showToast
import com.mingz.share.ui.PatternDrawing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PatternFragment : SafetyVerifyFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    override val mTag = TAG
    private lateinit var binding: FragmentPatternBinding

    companion object {
        const val TAG = CFG_PATTERN_BOOL
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPatternBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {
        // 设置回调
        binding.pattern.setCallback(object : PatternDrawing.Callback {
            override fun onResult(pattern: IntArray) {
                // 验证图案
                launch {
                    val context = context ?: return@launch
                    val safeKey = PatternActivity.decryptSafeKey(context, pattern)
                    if (safeKey == null) { // 图案错误
                        onLess()
                    } else { // 图案正确
                        SafeKey.init(AES.generateKey(safeKey))
                        onVerifySuccess()
                    }
                }
            }

            // 视为图案错误
            override fun onLess() {
                context?.showToast(PatternActivity.MESSAGE_PATTERN_ERROR)
                // 短暂延时后重新绘制
                binding.pattern.postDelayed({
                    binding.pattern.reDraw()
                }, PatternActivity.DELAY_REDRAW)
            }
        })
    }
}