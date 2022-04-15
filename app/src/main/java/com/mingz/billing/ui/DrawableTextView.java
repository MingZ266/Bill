package com.mingz.billing.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.appcompat.widget.AppCompatTextView;
import com.mingz.billing.R;

public class DrawableTextView extends AppCompatTextView {
    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
        try {
            Drawable drawableStart = setDrawableSize(typedArray,
                    R.styleable.DrawableTextView_android_drawableStart,
                    R.styleable.DrawableTextView_drawableStartWidth,
                    R.styleable.DrawableTextView_drawableStartHeight);
            Drawable drawableEnd = setDrawableSize(typedArray,
                    R.styleable.DrawableTextView_android_drawableEnd,
                    R.styleable.DrawableTextView_drawableEndWidth,
                    R.styleable.DrawableTextView_drawableEndHeight);
            Drawable drawableTop = setDrawableSize(typedArray,
                    R.styleable.DrawableTextView_android_drawableTop,
                    R.styleable.DrawableTextView_drawableTopWidth,
                    R.styleable.DrawableTextView_drawableTopHeight);
            Drawable drawableBottom = setDrawableSize(typedArray,
                    R.styleable.DrawableTextView_android_drawableBottom,
                    R.styleable.DrawableTextView_drawableBottomWidth,
                    R.styleable.DrawableTextView_drawableBottomHeight);
            setCompoundDrawables(drawableStart, drawableTop, drawableEnd, drawableBottom);
        } finally {
            typedArray.recycle();
        }
    }

    private Drawable setDrawableSize(TypedArray typedArray, @StyleableRes int drawableRes,
                                     @StyleableRes int widthRes, @StyleableRes int heightRes) {
        Drawable drawable = typedArray.getDrawable(drawableRes);
        if (drawable != null) {
            int width = (int) typedArray.getDimension(widthRes, -1);
            int height = (int) typedArray.getDimension(heightRes, -1);
            if (width < 0) {
                width = drawable.getMinimumWidth();
            }
            if (height < 0) {
                height = drawable.getMinimumHeight();
            }
            drawable.setBounds(0, 0, width, height);
        }
        return drawable;
    }
}
