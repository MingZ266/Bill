package com.mingz.share.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import com.mingz.share.R;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;
import java.util.Locale;

/**
 * 日期时间选择器.
 *
 *
 */
public class DateTimePicker extends LinearLayout {
    /**
     * 显示日期选择器和时间选择器.
     */
    public static final int MODE_ALL = 0;

    /**
     * 只显示日期选择器.
     */
    public static final int MODE_DATE = 1;

    /**
     * 只显示时间选择器.
     */
    public static final int MODE_TIME = 2;

    private final NumberPicker yearPicker;
    private final NumberPicker monthPicker;
    private final NumberPicker dayPicker;
    private final NumberPicker hourPicker;
    private final NumberPicker minutePicker;
    private final TextView dateSplitLeft;
    private final TextView dateSplitRight;
    private final TextView timeSplit;
    private final int timeMarginStart;

    @MODE
    private int lastMode;

    private OnDateTimeChangeListener listener = null;

    public DateTimePicker(Context context) {
        this(context, null);
    }

    public DateTimePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        yearPicker = new NumberPicker(context, attrs);
        monthPicker = new NumberPicker(context, attrs);
        dayPicker = new NumberPicker(context, attrs);
        hourPicker = new NumberPicker(context, attrs);
        minutePicker = new NumberPicker(context, attrs);
        dateSplitLeft = new TextView(context, attrs);
        dateSplitRight = new TextView(context, attrs);
        timeSplit = new TextView(context, attrs);
        final NumberPicker.Formatter formatter = value ->
                String.format(Locale.getDefault(), "%02d", value);
        // 年选择器
        yearPicker.setWrapSelectorWheel(false);
        settingYearRange(1970);
        yearPicker.setValue(1970);
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (monthPicker.getValue() == 2) {
                dayPicker.setMaxValue(getDayMaxValueOnFeb(newVal));
            }
            if (listener != null) {
                listener.onDate(newVal, monthPicker.getValue(), dayPicker.getValue());
                listener.onDateTime(newVal, monthPicker.getValue(), dayPicker.getValue(),
                        hourPicker.getValue(), minutePicker.getValue());
            }
        });
        // 月选择器
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setFormatter(formatter);
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            dayPicker.setMaxValue(getDayMaxValue(yearPicker.getValue(), newVal));
            if (listener != null) {
                listener.onDate(yearPicker.getValue(), newVal, dayPicker.getValue());
                listener.onDateTime(yearPicker.getValue(), newVal, dayPicker.getValue(),
                        hourPicker.getValue(), minutePicker.getValue());
            }
        });
        // 日选择器
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setFormatter(formatter);
        dayPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (listener != null) {
                listener.onDate(yearPicker.getValue(), monthPicker.getValue(), newVal);
                listener.onDateTime(yearPicker.getValue(), monthPicker.getValue(), newVal,
                        hourPicker.getValue(), minutePicker.getValue());
            }
        });
        // 时选择器
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setFormatter(formatter);
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (listener != null) {
                listener.onTime(newVal, minutePicker.getValue());
                listener.onDateTime(yearPicker.getValue(), monthPicker.getValue(), dayPicker.getValue(),
                        newVal, minutePicker.getValue());
            }
        });
        // 分选择器
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(formatter);
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (listener != null) {
                listener.onTime(hourPicker.getValue(), newVal);
                listener.onDateTime(yearPicker.getValue(), monthPicker.getValue(), dayPicker.getValue(),
                        hourPicker.getValue(), newVal);
            }
        });
        // 计算参数
        timeMarginStart = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                20.0f, context.getResources().getDisplayMetrics()));
        // 设置父组件参数
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        // 添加子组件
        yearPicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(yearPicker);

        dateSplitLeft.setText(" - ");
        addView(dateSplitLeft);

        monthPicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(monthPicker);

        dateSplitRight.setText(" - ");
        addView(dateSplitRight);

        dayPicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(dayPicker);

        LayoutParams hourParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        hourParams.setMarginStart(timeMarginStart);
        hourPicker.setLayoutParams(hourParams);
        addView(hourPicker);

        timeSplit.setText(" : ");
        addView(timeSplit);

        minutePicker.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        addView(minutePicker);

        // 设置模式
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DateTimePicker);
        final int mode;
        try {
            mode = typedArray.getInt(R.styleable.DateTimePicker_pickerMode, MODE_ALL);
        } finally {
            typedArray.recycle();
        }
        lastMode = MODE_ALL;
        setMode(mode);
    }

    private void settingYearRange(int year) {
        int lowLimit = year / 10 * 10 - 100;
        yearPicker.setMinValue(lowLimit);
        yearPicker.setMaxValue(lowLimit + 200);
    }

    private int getDayMaxValueOnFeb(int year) {
        if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
            return 29;
        } else {
            return 28;
        }
    }

    private int getDayMaxValue(int year, int month) {
        switch (month) {
            case 2:
                return getDayMaxValueOnFeb(year);
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
        }
        throw new IllegalArgumentException("月份应该在1到12之间");
    }

    private void hideDate() {
        yearPicker.setVisibility(View.GONE);
        monthPicker.setVisibility(View.GONE);
        dayPicker.setVisibility(View.GONE);
        dateSplitLeft.setVisibility(View.GONE);
        dateSplitRight.setVisibility(View.GONE);
        LayoutParams params = (LayoutParams) hourPicker.getLayoutParams();
        params.setMarginStart(0);
        hourPicker.setLayoutParams(params);
    }

    private void showDate() {
        yearPicker.setVisibility(View.VISIBLE);
        monthPicker.setVisibility(View.VISIBLE);
        dayPicker.setVisibility(View.VISIBLE);
        dateSplitLeft.setVisibility(View.VISIBLE);
        dateSplitRight.setVisibility(View.VISIBLE);
        LayoutParams params = (LayoutParams) hourPicker.getLayoutParams();
        params.setMarginStart(timeMarginStart);
        hourPicker.setLayoutParams(params);
    }

    private void hideTime() {
        hourPicker.setVisibility(View.GONE);
        minutePicker.setVisibility(View.GONE);
        timeSplit.setVisibility(View.GONE);
    }

    private void showTime() {
        hourPicker.setVisibility(View.VISIBLE);
        minutePicker.setVisibility(View.VISIBLE);
        timeSplit.setVisibility(View.VISIBLE);
    }

    public void setMode(@MODE int mode) {
        if (mode == lastMode) {
            return;
        }
        switch (mode) {
            case MODE_ALL:
                if (lastMode == MODE_DATE) {
                    showTime();
                } else {
                    showDate();
                }
                break;
            case MODE_DATE:
                if (lastMode == MODE_TIME) {
                    showDate();
                }
                hideTime();
                break;
            case MODE_TIME:
                if (lastMode == MODE_DATE) {
                    showTime();
                }
                hideDate();
                break;
        }
        lastMode = mode;
    }

    public void updateToNowTime() {
        Calendar now = Calendar.getInstance();
        setDateTime(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE));
    }

    public void setDateTime(@IntRange(from = 100) int year,
                             @IntRange(from = 1, to = 12) int month,
                             @IntRange(from = 1, to = 31) int day,
                             @IntRange(from = 0, to = 23) int hour,
                             @IntRange(from = 0, to = 59) int minute) {
        settingYearRange(year);
        yearPicker.setValue(year);
        monthPicker.setValue(month);
        int dayMaxValue = getDayMaxValue(year, month);
        if (day > dayMaxValue) {
            day = dayMaxValue;
        }
        dayPicker.setMaxValue(dayMaxValue);
        dayPicker.setValue(day);
        hourPicker.setValue(hour);
        minutePicker.setValue(minute);
    }

    public void setDate(@IntRange(from = 100) int year,
                        @IntRange(from = 1, to = 12) int month,
                        @IntRange(from = 1, to = 31) int day) {
        setDateTime(year, month, day, hourPicker.getValue(), minutePicker.getValue());
    }

    public int getYear() {
        return yearPicker.getValue();
    }

    public int getMonth() {
        return monthPicker.getValue();
    }

    public int getDay() {
        return dayPicker.getValue();
    }

    public int getHour() {
        return hourPicker.getValue();
    }

    public int getMinute() {
        return minutePicker.getValue();
    }

    public long getTimeInMillis() {
        Calendar current = Calendar.getInstance();
        current.set(yearPicker.getValue(), monthPicker.getValue() - 1, dayPicker.getValue(),
                hourPicker.getValue(), minutePicker.getValue());
        return current.getTimeInMillis();
    }

    public void setOnDateTimeChangeListener(OnDateTimeChangeListener listener) {
        this.listener = listener;
    }

    public interface OnDateTimeChangeListener {
        default void onDate(int year, int month, int day) {}

        default void onTime(int hour, int minute) {}

        default void onDateTime(int year, int month, int day, int hour, int minute) {}
    }

    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_ALL, MODE_DATE, MODE_TIME})
    @interface MODE {}
}
