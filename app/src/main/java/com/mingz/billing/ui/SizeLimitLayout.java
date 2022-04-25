package com.mingz.billing.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mingz.billing.R;

public class SizeLimitLayout extends FrameLayout {
    private final int maxWidth;
    private final int maxHeight;
    private final Float widthScale;
    private final Float heightScale;

    public SizeLimitLayout(@NonNull Context context) {
        this(context, null);
    }

    public SizeLimitLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        maxWidth = dm.widthPixels;
        maxHeight = dm.heightPixels;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SizeLimitLayout);
        try {
            if (typedArray.getType(R.styleable.SizeLimitLayout_widthScale) == TypedValue.TYPE_NULL) {
                widthScale = null;
            } else {
                float wScale = typedArray.getFloat(R.styleable.SizeLimitLayout_widthScale, 1.0f);
                widthScale = Math.min(1.0f, Math.max(wScale, 0.0f));
            }
            if (typedArray.getType(R.styleable.SizeLimitLayout_heightScale) == TypedValue.TYPE_NULL) {
                heightScale = null;
            } else {
                float hScale = typedArray.getFloat(R.styleable.SizeLimitLayout_heightScale, 1.0f);
                heightScale = Math.min(1.0f, Math.max(hScale, 0.0f));
            }
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthScale != null && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int widthLimit = (int) (maxWidth * widthScale);
            if (width > widthLimit) {
                width = widthLimit;
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        if (heightScale != null && MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int heightLimit = (int) (maxHeight * heightScale);
            if (height > heightLimit) {
                height = heightLimit;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
