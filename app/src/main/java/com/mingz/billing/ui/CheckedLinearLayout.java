package com.mingz.billing.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.mingz.billing.R;

public class CheckedLinearLayout extends LinearLayout implements Checkable {
    private boolean mChecked = false;

    public CheckedLinearLayout(Context context) {
        this(context, null);
    }

    public CheckedLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CheckedLinearLayout);
        try {
            setChecked(typedArray.getBoolean(
                    R.styleable.CheckedLinearLayout_android_checked, false));
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState;
        if (isChecked()) {
            drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, new int[] { android.R.attr.state_checked });
        } else {
            drawableState = super.onCreateDrawableState(extraSpace);
        }
        return drawableState;
    }
}
