package com.mingz.billing.ui

import android.content.Context
import android.util.AttributeSet

class HalfSwitchAnimView(context: Context, attrs: AttributeSet? = null
) : SwitchAnimView(context, attrs) {
    @Type
    fun getType() = type

    @Direction
    fun getDirection() = direction

    /**
     * 不使用[SwitchAnimView.AnimListener.onSwitch].
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
     * 不使用[SwitchAnimView.AnimListener.onSwitch].
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

    private fun refresh() {
        invalidate()
        requestLayout()
    }
}