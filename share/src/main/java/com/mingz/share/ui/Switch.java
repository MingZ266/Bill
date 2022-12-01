package com.mingz.share.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Checkable;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.mingz.share.R;

import java.util.ArrayList;

public class Switch extends View implements Checkable {
    private final AnimatorSet animatorSet; // 圆心移动动画、轨道颜色变化动画、圆颜色变化动画
    private final Param start; // 未选中时的参数
    private final Param end; // 选中时的参数
    private float animatorValue; // 当前圆心移动比例
    @ColorInt
    private int trackColor; // 当前轨道颜色
    @ColorInt
    private int thumbColor; // 当前圆的颜色

    private final Paint paint = new Paint();
    private final Path path = new Path();
    private boolean checked; // 是否选中
    private final boolean autoToggle; // 当点击时，是否自动切换
    private final float radius; // 圆的半径
    private final float space; // 圆与轨道之间间隙的宽度
    private final float drawWidth; // 绘制区域的宽度
    private final float drawHeight; // 绘制区域的高度

    @Nullable
    private OnClickListener onClickListener; // 记录设置的点击监听
    @Nullable
    private OnCheckedChangeListener onCheckedChangeListener; // 选择改变监听

    public Switch(Context context) {
        this(context, null);
    }

    public Switch(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Switch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Switch(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // 1dp的大小
        final float oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f,
                context.getResources().getDisplayMetrics());
        // 读取设置的值
        ColorStateList trackColorStateList;
        ColorStateList thumbColorStateList;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Switch, defStyleAttr, defStyleRes);
        try {
            checked = typedArray.getBoolean(R.styleable.Switch_android_checked, false);
            autoToggle = typedArray.getBoolean(R.styleable.Switch_autoToggle, true);
            radius = typedArray.getDimension(R.styleable.Switch_radius, 10.0f * oneDp);
            trackColorStateList = typedArray.getColorStateList(R.styleable.Switch_trackColor);
            thumbColorStateList = typedArray.getColorStateList(R.styleable.Switch_thumbColor);
        } finally {
            typedArray.recycle();
        }
        // 初始化参数
        space = radius / 5.0f;
        drawWidth = radius * 4.0f + 2.0f * space; // 两个圆的直径加两边的间隙
        drawHeight = radius * 2.0f + 2.0f * space; // 一个圆的直径加两边的间隙
        // 获取起止值
        final int[] stateWhenChecked = new int[] { android.R.attr.state_checked };
        final int[] stateWhenNotChecked = new int[] { - android.R.attr.state_checked };
        start = new Param(0.0f, getColor(trackColorStateList, stateWhenNotChecked, Color.GRAY),
                getColor(thumbColorStateList, stateWhenNotChecked, Color.WHITE));
        end = new Param(1.0f, getColor(trackColorStateList, stateWhenChecked, Color.GRAY),
                getColor(thumbColorStateList, stateWhenChecked, Color.WHITE));
        // 初始化动画
        ArrayList<Animator> set = new ArrayList<>(3);
        // 圆心移动动画
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start.value, end.value);
        valueAnimator.addUpdateListener(animation -> {
            animatorValue = (float) animation.getAnimatedValue();
            // 刷新视图
            invalidate();
            requestLayout();
        });
        set.add(valueAnimator);
        // 轨道的颜色变化动画
        if (start.trackColor != end.trackColor) {
            ValueAnimator trackColorAnimator = ValueAnimator.ofArgb(start.trackColor, end.trackColor);
            trackColorAnimator.addUpdateListener(animation -> trackColor = (int) animation.getAnimatedValue());
            set.add(trackColorAnimator);
        }
        // 圆的颜色变化动画
        if (start.thumbColor != end.thumbColor) {
            ValueAnimator thumbColorAnimator = ValueAnimator.ofArgb(start.thumbColor, end.thumbColor);
            thumbColorAnimator.addUpdateListener(animation -> thumbColor = (int) animation.getAnimatedValue());
            set.add(thumbColorAnimator);
        }
        animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.playTogether(set);
        // 初始化绘制参数
        initParamFromChecked();
        super.setOnClickListener(v -> {
            if (autoToggle) toggle();
            // 调用记录的点击监听
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
        });
    }

    private void initParamFromChecked() {
        if (checked) {
            animatorValue = end.value;
            trackColor = end.trackColor;
            thumbColor = end.thumbColor;
        } else {
            animatorValue = start.value;
            trackColor = start.trackColor;
            thumbColor = start.thumbColor;
        }
    }

    /**
     * 初始化组件是否选中而不触发相关监听及动画.
     * @param checked 组件是否选中
     */
    public void initChecked(boolean checked) {
        if (this.checked == checked) return;
        this.checked = checked;
        initParamFromChecked();
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int minW = (int) drawWidth + getPaddingLeft() + getPaddingRight();
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int minH = (int) drawHeight + getPaddingTop() + getPaddingBottom();
        if (wMode != MeasureSpec.EXACTLY || wSize < minW) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(minW, MeasureSpec.EXACTLY);
        }
        if (hMode != MeasureSpec.EXACTLY || hSize < minH) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(minH, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 计算绘制区域
        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = left + drawWidth;
        float bottom = top + drawHeight;
        // 绘制轨道
        float trackR = radius + space; // 轨道圆弧半径
        float trackD = trackR * 2.0f; // 轨道圆弧直径
        path.reset();
        // 左半圆
        path.addArc(left, top, left + trackD, bottom, 90.0f, 180.0f);
        // 右半圆
        path.addArc(right - trackD, top, right, bottom, - 90.0f, 180.0f);
        // 中间的矩形
        path.addRect(left + trackR, top, right - trackR, bottom, Path.Direction.CW);
        paint.reset();
        paint.setColor(trackColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawPath(path, paint);
        // 绘制圆
        paint.setColor(thumbColor);
        float distance = drawWidth - trackD; // 圆心的移动距离
        float cx = left + trackR + distance * animatorValue; // 圆心的x坐标
        canvas.drawCircle(cx, top + trackR, radius, paint);
    }

    // 根据state中的状态获取颜色，若colorStateList为空或找不到适合的颜色时返回defColor.
    private int getColor(@Nullable ColorStateList colorStateList, int[] state, int defColor) {
        if (colorStateList == null) return defColor;
        return colorStateList.getColorForState(state, defColor);
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked == checked) return;
        toggle();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        if (checked) { // 逆向播放动画以取消选中
            animatorSet.reverse();
        } else { // 正向播放动画以选中
            animatorSet.start();
        }
        checked = !checked;
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(checked);
        }
    }

    // 覆写以记录设置的监听，避免设置的监听造成覆盖
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
    }

    /**
     * 设置选择改变监听.
     */
    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener l) {
        onCheckedChangeListener = l;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isChecked);
    }

    // 用于记录起止的绘制参数
    private static class Param {
        private final float value; // 圆心移动比例
        @ColorInt
        private final int trackColor; // 轨道颜色
        @ColorInt
        private final int thumbColor; // 圆的颜色

        private Param(float value, @ColorInt int trackColor, @ColorInt int thumbColor) {
            this.value = value;
            this.trackColor = trackColor;
            this.thumbColor = thumbColor;
        }
    }
}
