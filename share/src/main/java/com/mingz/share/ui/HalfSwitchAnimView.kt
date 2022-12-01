package com.mingz.share.ui

import android.content.Context
import android.util.AttributeSet

/**
 * 执行切换动画过程的一部分.
 */
class HalfSwitchAnimView(context: Context, attrs: AttributeSet? = null
) : SwitchAnimView(context, attrs) {

    /**
     * 获取动画类型.
     */
    @Type
    fun getType() = type

    /**
     * 获取动画方向.
     */
    @Direction
    fun getDirection() = direction

    /**
     * 执行切换动画的前半部分.
     *
     * 不会调用[SwitchAnimView.AnimListener.onSwitch].
     */
    fun startAnimBeforeSwitch(listener: AnimListener? = null) {
        type = TYPE_RANDOM
        direction = DIRECTION_RANDOM
        setRandomTypeIfNeed()
        setRandomDirectionIfNeed()
        val animator = getAnimator(duration / 2, 0.0f, 1.0f)
        animator.addUpdateListener { animation ->
            value = animation.animatedValue as Float
            if (value == 1.0f) {
                listener?.onStop()
            }
            refresh()
        }
        phaseI = true
        animator.start()
        listener?.onStart()
    }

    /**
     * 执行切换动画的后半部分.
     *
     * 不会调用[SwitchAnimView.AnimListener.onSwitch].
     */
    fun startAnimAfterSwitch(@Type type: Int, @Direction direction: Int, listener: AnimListener? = null) {
        this.type = type
        this.direction = direction
        setRandomTypeIfNeed()
        setRandomDirectionIfNeed()
        val animator = getAnimator(duration / 2, 1.0f, 0.0f)
        animator.addUpdateListener { animation ->
            value = animation.animatedValue as Float
            if (value == 0.0f) {
                listener?.onStop()
            }
            refresh()
        }
        phaseI = false
        animator.start()
        listener?.onStart()
    }
}