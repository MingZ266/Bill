package com.mingz.billing.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import com.mingz.billing.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Random;

public class SwitchAnimView extends View {
    protected final long duration = 800;
    @FloatRange(from = 0.0f, to = 1.0f)
    protected float value = 0.0f; // 动画进度值
    protected boolean phaseI = true; // 是否为动画的第一阶段
    @Type
    protected int type = TYPE_RANDOM; // 动画类型
    @Direction
    protected int direction = DIRECTION_RANDOM; // 滑入方向
    @ColorInt
    private final int maskColor; // 遮罩层颜色
    private final Path path = new Path();
    private final Paint hardPaint = new Paint();

    /**
     * 随机动画.
     */
    public static final int TYPE_RANDOM = 0;

    /**
     * 遮罩层淡入、淡出.
     */
    public static final int TYPE_FADE = 1;

    /**
     * 遮罩层以圆形覆盖、揭开.
     */
    public static final int TYPE_CIRCLE = 2;

    /**
     * 遮罩层以滑块扫描方式滑入、滑出.
     */
    public static final int TYPE_SLIDER_SCAN = 3;

    /**
     * 遮罩层以滑块回弹方式滑入、滑出.
     */
    public static final int TYPE_SLIDER_ELASTIC = 4;

    /**
     * 对{@link #TYPE_SLIDER_SCAN}和{@link #TYPE_SLIDER_ELASTIC}类型的动画应用随机方向.
     */
    public static final int DIRECTION_RANDOM = 0;

    public SwitchAnimView(Context context) {
        this(context, null);
    }

    public SwitchAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchAnimView);
        try {
            maskColor = typedArray.getColor(R.styleable.SwitchAnimView_maskColor, Color.WHITE) | 0xFF000000;
        } finally {
            typedArray.recycle();
        }
        setLayerType(LAYER_TYPE_HARDWARE, hardPaint);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    protected void onDraw(Canvas canvas) {
        if (value <= 0.0f) {
            return;
        }
        switch (type) {
            case TYPE_FADE:
                drawFade(canvas);
                break;
            case TYPE_CIRCLE:
                drawCircle(canvas);
                break;
            case TYPE_SLIDER_SCAN:
                drawSliderScan(canvas);
                break;
            case TYPE_SLIDER_ELASTIC:
                drawSliderElastic(canvas);
                break;
        }
    }

    private void drawFade(Canvas canvas) {
        int alpha = (int) (255.0f * value);
        hardPaint.setColor(maskColor & 0x00FFFFFF | (alpha << 24));
        canvas.drawPaint(hardPaint);
    }

    private void drawCircle(Canvas canvas) {
        float centerX = getWidth() / 2.0f;
        float centerY = getHeight() / 2.0f;
        float radius = Math.max(centerX, centerY) * (1.0f - value);
        path.reset();
        path.addCircle(centerX, centerY, radius, Path.Direction.CW);
        canvas.save();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(path);
        } else {
            //noinspection deprecation
            canvas.clipPath(path, Region.Op.DIFFERENCE);
        }
        canvas.drawColor(maskColor);
        canvas.restore();
    }

    @SuppressLint("SwitchIntDef")
    private void drawSliderScan(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        path.reset();
        float size;
        switch (direction) {
            case Gravity.START:
                size = width * value;
                if (phaseI) {
                    path.addRect(0.0f, 0.0f, size, height, Path.Direction.CW);
                } else {
                    path.addRect(width - size, 0.0f, width, height, Path.Direction.CW);
                }
                break;
            case Gravity.END:
                size = width * value;
                if (phaseI) {
                    path.addRect(width - size, 0.0f, width, height, Path.Direction.CW);
                } else {
                    path.addRect(0.0f, 0.0f, size, height, Path.Direction.CW);
                }
                break;
            case Gravity.TOP:
                size = height * value;
                if (phaseI) {
                    path.addRect(0.0f, 0.0f, width, size, Path.Direction.CW);
                } else {
                    path.addRect(0.0f, height - size, width, height, Path.Direction.CW);
                }
                break;
            case Gravity.BOTTOM:
                size = height * value;
                if (phaseI) {
                    path.addRect(0.0f, height - size, width, height, Path.Direction.CW);
                } else {
                    path.addRect(0.0f, 0.0f, width, size, Path.Direction.CW);
                }
                break;
        }
        canvas.save();
        canvas.clipPath(path);
        canvas.drawColor(maskColor);
        canvas.restore();
    }

    @SuppressLint("SwitchIntDef")
    private void drawSliderElastic(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        path.reset();
        switch (direction) {
            case Gravity.START:
                path.addRect(0.0f, 0.0f, width * value, height, Path.Direction.CW);
                break;
            case Gravity.END:
                path.addRect(width * (1.0f - value), 0.0f, width, height, Path.Direction.CW);
                break;
            case Gravity.TOP:
                path.addRect(0.0f, 0.0f, width, height * value, Path.Direction.CW);
                break;
            case Gravity.BOTTOM:
                path.addRect(0.0f, height * (1.0f - value), width, height, Path.Direction.CW);
                break;
        }
        canvas.save();
        canvas.clipPath(path);
        canvas.drawColor(maskColor);
        canvas.restore();
    }

    public void startAnim(@Nullable AnimListener listener) {
        direction = DIRECTION_RANDOM;
        startAnim(TYPE_RANDOM, listener);
    }

    public void startAnim(@Type int type, @Nullable AnimListener listener) {
        this.type = type;
        setRandomTypeIfNeed();
        setRandomDirectionIfNeed();
        ValueAnimator animator = getAnimator(duration, 0.0f, 1.0f, 2.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private boolean notifySwitch = true;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                phaseI = v < 1.0f;
                if (phaseI) {
                    value = v;
                } else {
                    value = 2.0f - v;
                    if (listener != null) {
                        if (notifySwitch) {
                            notifySwitch = false;
                            listener.onSwitch();
                        }
                        if (v == 2.0f) {
                            listener.onStop();
                        }
                    }
                }
                invalidate();
                requestLayout();
            }
        });
        phaseI = true;
        animator.start();
        if (listener != null) {
            listener.onStart();
        }
    }

    /**
     * 设置当动画类型为{@link #TYPE_SLIDER_SCAN}或{@link #TYPE_SLIDER_ELASTIC}时的方向.
     */
    public void setDirection(@Direction int direction) {
        this.direction = direction;
    }

    protected ValueAnimator getAnimator(long duration, float... values) {
        ValueAnimator animator = ValueAnimator.ofFloat(values);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    protected void setRandomTypeIfNeed() {
        if (type == TYPE_RANDOM) {
            switch (new Random().nextInt(4)) {
                case 0:
                    type = TYPE_FADE;
                    break;
                case 1:
                    type = TYPE_CIRCLE;
                    break;
                case 2:
                    type = TYPE_SLIDER_SCAN;
                    break;
                case 3:
                    type = TYPE_SLIDER_ELASTIC;
                    break;
            }
        }
    }

    protected void setRandomDirectionIfNeed() {
        if (direction == DIRECTION_RANDOM && (type == TYPE_SLIDER_SCAN
                || type == TYPE_SLIDER_ELASTIC)) {
            switch (new Random().nextInt(4)) {
                case 0:
                    direction = Gravity.START;
                    break;
                case 1:
                    direction = Gravity.END;
                    break;
                case 2:
                    direction = Gravity.TOP;
                    break;
                case 3:
                    direction = Gravity.BOTTOM;
                    break;
            }
        }
    }

    public static abstract class AnimListener {
        /**
         * 动画已经开始.
         */
        public void onStart() {}

        /**
         * 动画已经切换.
         */
        public void onSwitch() {}

        /**
         * 动画已经结束.
         */
        public void onStop() {}
    }

    @IntDef({ TYPE_RANDOM, TYPE_FADE, TYPE_CIRCLE, TYPE_SLIDER_SCAN, TYPE_SLIDER_ELASTIC})
    @Retention(RetentionPolicy.SOURCE)
    protected @interface Type {}

    @IntDef({ DIRECTION_RANDOM, Gravity.START, Gravity.END, Gravity.TOP, Gravity.BOTTOM })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface Direction {}
}
