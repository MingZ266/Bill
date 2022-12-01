package com.mingz.share.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Checkable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import com.mingz.share.R;

public class CheckedImageView extends AppCompatImageView implements Checkable {
    private boolean checked = false;

    public CheckedImageView(@NonNull Context context) {
        this(context, null);
    }

    public CheckedImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CheckedImageView);
        try {
            setChecked(typedArray.getBoolean(R.styleable.CheckedImageView_android_checked, false));
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    // 添加checked状态
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
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
